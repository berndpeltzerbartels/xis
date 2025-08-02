package one.xis.auth;

import lombok.RequiredArgsConstructor;
import one.xis.auth.idp.ExternalIDPServices;
import one.xis.context.AppContext;
import one.xis.http.*;
import one.xis.utils.lang.ClassUtils;

import java.lang.reflect.Modifier;

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
        appContext.getOptionalSingleton(UserInfoService.class)
                .ifPresent(userInfoService -> saveUserInfo(externalTokens.getIdToken(), userInfoService, stateParameterPayload.getIssuer()));
        return ResponseEntity.redirect(stateParameterPayload.getRedirect())
                .addSecureCookie("access_token", externalTokens.getAccessToken(), externalTokens.getExpiresInSeconds())
                .addSecureCookie("refresh_token", externalTokens.getRefreshToken(), externalTokens.getRefreshExpiresInSeconds());
    }


    /**
     * Saves the user information based on the provided ID token.
     * It retrieves the user info from the ID token claims and saves it using the UserInfoService.
     * If the user info already exists, it updates it; otherwise, it creates a new user info object.
     *
     * @param idToken         The ID token containing user information.
     * @param userInfoService The service used to manage user information.
     * @param <U>             The type of UserInfo.
     * @param issuer          The issuer
     */
    private <U extends UserInfo> void saveUserInfo(String idToken, UserInfoService<U> userInfoService, String issuer) {
        var keyId = tokenService.extractKeyId(idToken);
        var jsonWebKey = externalIDPServices.getExternalIDPService(issuer).getJsonWebKey(keyId);
        var idTokenClaims = tokenService.decodeIdToken(idToken, jsonWebKey);
        userInfoService.getUserInfo(idTokenClaims.getUserId()).ifPresentOrElse(
                userInfo -> updateUserInfo(idTokenClaims, userInfo, userInfoService),
                () -> saveNewUserInfo(idTokenClaims, userInfoService)
        );
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
     * Saves a new user info object based on the provided ID token claims.
     * This method maps the ID token claims to a new user info object and saves it using the provided UserInfoService.
     * It is used to create a new user profile after successful authentication.
     *
     * @param idTokenClaims
     * @param userInfoService
     * @param <U>
     */
    <U extends UserInfo> void saveNewUserInfo(IDTokenClaims idTokenClaims, UserInfoService<U> userInfoService) {
        var userInfoImplementationClass = getUserInfoClass(userInfoService);
        if (userInfoImplementationClass.isInterface() || Modifier.isAbstract(userInfoImplementationClass.getModifiers())) {
            throw new IllegalStateException("Type parameter of UserInfoService implementation class must be a concrete implementation of UserInfo : " + userInfoImplementationClass.getName());
        }
        var userInfo = ClassUtils.newInstance(userInfoImplementationClass);
        mapToUserInfo(idTokenClaims, userInfo);
        userInfoService.saveUserInfo(userInfo);
    }

    /**
     * Updates the user info with the provided ID token claims.
     * This method maps the ID token claims to the user info object and saves it using the provided UserInfoService.
     * It is used to update the user profile information after successful authentication.
     *
     * @param idTokenClaims
     * @param userInfo
     * @param userInfoService
     * @param <U>
     */
    <U extends UserInfo> void updateUserInfo(IDTokenClaims idTokenClaims, U userInfo, UserInfoService<U> userInfoService) {
        mapToUserInfo(idTokenClaims, userInfo);
        userInfoService.saveUserInfo(userInfo);
    }


    /**
     * Retrieves the class of the UserInfo object from the UserInfoService.
     *
     * @param userInfoService The UserInfoService instance.
     * @return The class of the UserInfo object.
     */
    @SuppressWarnings("unchecked")
    private <U extends UserInfo> Class<U> getUserInfoClass(UserInfoService<U> userInfoService) {
        return (Class<U>) ClassUtils.getGenericInterfacesTypeParameter(userInfoService.getClass(), UserInfoService.class, 0);
    }

    /**
     * Maps the ID token claims to the user info object.
     *
     * @param claims   The ID token claims containing user information.
     * @param userInfo The user info object to be populated with the claims.
     */
    private void mapToUserInfo(IDTokenClaims claims, UserInfo userInfo) {
        userInfo.setUserId(claims.getUserId());
        userInfo.setEmail(claims.getEmail());
        if (claims.getEmailVerified() != null) {
            userInfo.setEmailVerified(claims.getEmailVerified());
        }
        userInfo.setLocale(claims.getLocale());
        userInfo.setName(claims.getName());
        userInfo.setPreferredUsername(claims.getPreferredUsername());
        userInfo.setGivenName(claims.getGivenName());
        userInfo.setFamilyName(claims.getFamilyName());
        userInfo.setPictureUrl(claims.getPictureUrl());
    }
}
