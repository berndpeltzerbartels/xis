package one.xis.auth;

import lombok.RequiredArgsConstructor;
import one.xis.auth.idp.ExternalIDPServices;
import one.xis.auth.idp.ExternalIDPTokens;
import one.xis.context.AppContext;
import one.xis.http.*;
import one.xis.utils.lang.ClassUtils;

import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.Set;

/**
 * Controller for handling local authentication callbacks.
 * This controller is responsible for processing the authentication callback for external identity providers (IDPs)
 * and local authentication.
 * It retrieves the authorization code and state from the request parameters,
 * decodes and verifies the state, fetches new tokens using the IDP client service,
 * and returns the tokens along with a redirect URL.
 */
@Controller("/xis/auth")
@RequiredArgsConstructor
class AuthenticationController {

    private final TokenService tokenService;
    private final LocalTokenService localTokenService;
    private final AppContext appContext;
    private final ExternalIDPServices externalIDPServices;
    private final CodeStore codeStore;


    /**
     * Handles the authentication callback from an IDP.
     * It retrieves the authorization code and state from the request parameters,
     * decodes and verifies the state, fetches new tokens using the IDP client service,
     * and returns the tokens along with a redirect URL.
     * <p>
     * Because this endpoint is called by ajax, it does not return a redirect response.
     * The client has to handle the redirect programmatically, so instead of returning a redirect,
     * it returns a response entity with the redirect URL and tokens in secure cookies.
     *
     * @param code  The authorization code received from the IDP.
     * @param state The state parameter used to verify the request.
     * @return A response entity containing the API tokens and redirect URL.
     */
    @Get("/callback/{idpId}")
    public ResponseEntity<?> authenticationCallback(@UrlParameter("code") String code, @UrlParameter("state") String state) {
        try {
            StateParameterPayload stateParameterPayload = StateParameter.decodeAndVerify(state);
            if ("local".equals(stateParameterPayload.getIssuer())) {
                return handleLocalAuthentication(code, stateParameterPayload);
            } else {
                return handleExternalAuthentication(code, stateParameterPayload);
            }
        } catch (InvalidStateParameterException e) {
            return handleInvalidStateParameterException(e);
        }
    }


    private ResponseEntity<?> handleInvalidStateParameterException(InvalidStateParameterException e) {
        if (e.getStateParameterPayload() != null) {
            if ("local".equals(e.getStateParameterPayload().getIssuer())) {
                return ResponseEntity.redirect("/login.html?error=invalid_state_parameter");
            } else {
                var externalIDPService = externalIDPServices.getExternalIDPService(e.getStateParameterPayload().getIssuer());
                if (externalIDPService != null) {
                    return ResponseEntity.redirect(externalIDPService.createLoginUrl(e.getStateParameterPayload().getRedirect()));
                } else {
                    return ResponseEntity.status(401)
                            .body("Invalid state parameter: " + e.getMessage());
                }
            }
        }
        return ResponseEntity.status(401)
                .body("Authentication failed: " + e.getMessage());
    }

    /**
     * Handles the authentication callback for local authentication.
     * It retrieves the user ID associated with the provided code,
     * generates new access and refresh tokens, and returns them in secure cookies.
     *
     * @param code                  The authorization code received from the local authentication.
     * @param stateParameterPayload The state parameter payload containing redirect URL.
     * @return A response entity with secure cookies for access and refresh tokens.
     */
    private ResponseEntity<?> handleLocalAuthentication(String code, StateParameterPayload stateParameterPayload) {
        var userId = codeStore.getUserIdForCode(code);
        if (userId == null) {
            throw new AuthenticationException("No user found for code: " + code);
        }
        var tokens = localTokenService.newTokens(userId);
        return ResponseEntity.redirect(stateParameterPayload.getRedirect())
                .addSecureCookie("access_token", tokens.getAccessToken(), tokens.getAccessTokenExpiresIn())
                .addSecureCookie("refresh_token", tokens.getRenewToken(), tokens.getRenewTokenExpiresIn());
    }

    /**
     * Handles the authentication callback for external identity providers.
     * It fetches the tokens from the external IDP service and saves the user info if available.
     * The response includes secure cookies for access and refresh tokens.
     *
     * @param code                  The authorization code received from the IDP.
     * @param stateParameterPayload The state parameter payload containing issuer and redirect URL.
     * @return A response entity with secure cookies for access and refresh tokens.
     */
    @SuppressWarnings("unchecked")
    private ResponseEntity<?> handleExternalAuthentication(String code, StateParameterPayload stateParameterPayload) {
        var externalIDPService = externalIDPServices.getExternalIDPService(stateParameterPayload.getIssuer());
        if (externalIDPService == null) {
            throw new IllegalStateException("No IDP client service found for provider: " + stateParameterPayload.getIssuer());
        }
        var externalTokens = externalIDPService.fetchTokens(code);
        var tokens = createLocalTokensFromExternalIdentity(externalTokens, stateParameterPayload.getIssuer());
        return ResponseEntity.redirect(stateParameterPayload.getRedirect())
                .addSecureCookie("access_token", tokens.getAccessToken(), tokens.getAccessTokenExpiresIn())
                .addSecureCookie("refresh_token", tokens.getRenewToken(), tokens.getRenewTokenExpiresIn());
    }

    private ApiTokens createLocalTokensFromExternalIdentity(ExternalIDPTokens externalTokens, String issuer) {
        var userAccount = appContext.getOptionalSingleton(UserAccountService.class)
                .filter(userAccountService -> !(userAccountService instanceof UserServicePlaceholder))
                .map(userAccountService -> saveUserAccount(externalTokens.getIdToken(), userAccountService, issuer))
                .orElseGet(() -> createTransientUserAccount(externalTokens, issuer));
        return localTokenService.newTokens(userAccount);
    }


    /**
     * Saves the user account based on the provided ID token.
     * It retrieves the account from the ID token claims and saves it using the UserAccountService.
     * If the account already exists, it updates it; otherwise, it creates a new user account object.
     *
     * @param idToken         The ID token containing user information.
     * @param userAccountService The service used to manage user information.
     * @param <U>             The type of UserAccount.
     * @param issuer          The issuer
     */
    private <U extends UserAccount> U saveUserAccount(String idToken, UserAccountService<U> userAccountService, String issuer) {
        var keyId = tokenService.extractKeyId(idToken);
        var jsonWebKey = externalIDPServices.getExternalIDPService(issuer).getJsonWebKey(keyId);
        var idTokenClaims = tokenService.decodeIdToken(idToken, jsonWebKey);
        return userAccountService.getUserAccount(idTokenClaims.getUserId())
                .map(userAccount -> updateUserAccount(idTokenClaims, userAccount, userAccountService))
                .orElseGet(() -> saveNewUserAccount(idTokenClaims, userAccountService));
    }

    private UserAccount createTransientUserAccount(ExternalIDPTokens externalTokens, String issuer) {
        var idToken = externalTokens.getIdToken();
        var keyId = tokenService.extractKeyId(idToken);
        var jsonWebKey = externalIDPServices.getExternalIDPService(issuer).getJsonWebKey(keyId);
        var idTokenClaims = tokenService.decodeIdToken(idToken, jsonWebKey);
        var userAccount = new UserAccountImpl();
        mapToUserAccount(idTokenClaims, userAccount);
        userAccount.setRoles(readRolesFromExternalAccessToken(externalTokens.getAccessToken(), issuer));
        return userAccount;
    }

    private Set<String> readRolesFromExternalAccessToken(String accessToken, String issuer) {
        if (accessToken == null || accessToken.isBlank()) {
            return Set.of();
        }
        try {
            var keyId = tokenService.extractKeyId(accessToken);
            var jsonWebKey = externalIDPServices.getExternalIDPService(issuer).getJsonWebKey(keyId);
            return tokenService.decodeAccessToken(accessToken, jsonWebKey).getRoles();
        } catch (InvalidTokenException e) {
            return Set.of();
        }
    }

    /**
     * Renews the access and refresh tokens using the provided refresh token.
     * It decodes the refresh token, retrieves the user information, and generates new tokens.
     *
     * @param refreshToken The refresh token used to renew the tokens.
     * @return A response entity with no content, but with updated secure cookies for access and refresh tokens.
     */

    @Post("/token")
    public ResponseEntity<?> renewTokens(@CookieValue("refresh_token") String refreshToken) {
        var tokens = localTokenService.renewTokens(refreshToken);
        return ResponseEntity.noContent()
                .addSecureCookie("access_token", tokens.getAccessToken(), tokens.getAccessTokenExpiresIn())
                .addSecureCookie("refresh_token", tokens.getRenewToken(), tokens.getRenewTokenExpiresIn());
    }


    /**
     * Saves a new user account object based on the provided ID token claims.
     * This method maps the ID token claims to a new user account object and saves it using the provided UserAccountService.
     * It is used to create a new user profile after successful authentication.
     *
     * @param idTokenClaims
     * @param userAccountService
     * @param <U>
     */
    <U extends UserAccount> U saveNewUserAccount(IDTokenClaims idTokenClaims, UserAccountService<U> userAccountService) {
        var userAccountImplementationClass = getUserAccountClass(userAccountService);
        if (userAccountImplementationClass.isInterface() || Modifier.isAbstract(userAccountImplementationClass.getModifiers())) {
            throw new IllegalStateException("Type parameter of UserAccountService implementation class must be a concrete implementation of UserAccount : " + userAccountImplementationClass.getName());
        }
        var userAccount = ClassUtils.newInstance(userAccountImplementationClass);
        mapToUserAccount(idTokenClaims, userAccount);
        userAccountService.saveUserAccount(userAccount);
        return userAccount;
    }

    /**
     * Updates the user account with the provided ID token claims.
     * This method maps the ID token claims to the user account object and saves it using the provided UserAccountService.
     * It is used to update the user profile information after successful authentication.
     *
     * @param idTokenClaims
     * @param userAccount
     * @param userAccountService
     * @param <U>
     */
    <U extends UserAccount> U updateUserAccount(IDTokenClaims idTokenClaims, U userAccount, UserAccountService<U> userAccountService) {
        mapToUserAccount(idTokenClaims, userAccount);
        userAccountService.saveUserAccount(userAccount);
        return userAccount;
    }


    /**
     * Retrieves the class of the UserAccount object from the UserAccountService.
     *
     * @param userAccountService The UserAccountService instance.
     * @return The class of the UserAccount object.
     */
    @SuppressWarnings("unchecked")
    private <U extends UserAccount> Class<U> getUserAccountClass(UserAccountService<U> userAccountService) {
        return (Class<U>) ClassUtils.getGenericInterfacesTypeParameter(userAccountService.getClass(), UserAccountService.class, 0);
    }

    /**
     * Maps the ID token claims to the user account object.
     *
     * @param claims   The ID token claims containing user information.
     * @param userAccount The user account object to be populated with the claims.
     */
    private void mapToUserAccount(IDTokenClaims claims, UserAccount userAccount) {
        userAccount.setUserId(claims.getUserId());
        userAccount.setEmail(claims.getEmail());
        if (claims.getEmailVerified() != null) {
            userAccount.setEmailVerified(claims.getEmailVerified());
        }
        userAccount.setLocale(claims.getLocale());
        userAccount.setName(claims.getName());
        userAccount.setPreferredUsername(claims.getPreferredUsername());
        userAccount.setGivenName(claims.getGivenName());
        userAccount.setFamilyName(claims.getFamilyName());
        userAccount.setPictureUrl(claims.getPictureUrl());
    }
}
