package one.xis.auth.idp;

import lombok.RequiredArgsConstructor;
import one.xis.auth.*;
import one.xis.auth.token.TokenService;
import one.xis.context.XISDefaultComponent;
import one.xis.server.LocalUrlHolder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@XISDefaultComponent
@RequiredArgsConstructor
class IDPAuthenticationServiceImpl implements IDPAuthenticationService {

    private final IDPService idpService;
    private final TokenService tokenService;
    private final LocalUrlHolder localUrlHolder;
    private final IDPCodeStore idpCodeStore;


    /**
     * Logs in a user using the provided credentials.
     *
     * @param login the login credentials containing username and password
     * @return a unique authorization code for the user session
     * @throws InvalidCredentialsException if the provided credentials are invalid
     */
    @Override
    public String login(IDPServerLogin login) throws InvalidCredentialsException {
        if (!idpService.validateCredentials(login.getUsername(), login.getPassword())) {
            throw new InvalidCredentialsException();
        }
        String code = UUID.randomUUID().toString();
        idpCodeStore.store(code, login.getUsername());
        return code;
    }

    /**
     * Issues an access token based on the provided authorization code.
     *
     * @param code the authorization code received from the IDP server
     * @return an ApiTokens object containing the access and refresh tokens
     * @throws AuthenticationException if the code is invalid or expired
     */

    private IDPTokenResponse issueToken(String code) throws AuthenticationException {
        String userId = idpCodeStore.getUserIdForCode(code);
        if (userId == null) {
            throw new InvalidStateParameterException();
        }
        return generateTokenResponse(userId);
    }

    /**
     * Refreshes the access token using the provided refresh token.
     *
     * @param refreshToken the refresh token to use for generating a new access token
     * @return an ApiTokens object containing the new access and refresh tokens
     * @throws InvalidTokenException   if the refresh token is invalid or expired
     * @throws AuthenticationException if the user associated with the refresh token is not found
     */

    @Override
    public IDPTokenResponse refresh(String refreshToken) throws InvalidTokenException, AuthenticationException {
        String userId = verifyRefreshToken(refreshToken);
        return generateTokenResponse(userId);
    }


    /**
     * Checks if the provided redirect URL is valid for the given user ID.
     *
     * @param userId      the ID of the user
     * @param redirectUrl the redirect URL to validate
     * @throws InvalidRedirectUrlException if the redirect URL is not permitted for the user
     */
    @Override
    public void checkRedirectUrl(String userId, String redirectUrl) throws InvalidRedirectUrlException {
        if (!idpService.userInfo(userId)
                .map(IDPUserInfo::getClientId)
                .map(idpService::findClientInfo)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(IDPClientInfo::getPermittedRedirectUrls)
                .map(urls -> urls.contains(redirectUrl))
                .orElse(false)) {
            throw new InvalidRedirectUrlException(redirectUrl);
        }
    }

    @Override
    public IDPWellKnownOpenIdConfig getOpenIdConfigJson() {
        var config = new IDPWellKnownOpenIdConfig();
        config.setIssuer(localUrlHolder.getUrl());
        config.setJwksUri(localUrlHolder.getUrl() + "/.well-known/jwks.json"); // TODO create constants for these URLs
        config.setAuthorizationEndpoint(localUrlHolder.getUrl() + "/idp/login.html");
        config.setTokenEndpoint(localUrlHolder.getUrl() + "/xis/idp/tokens");
        // config.setUserInfoEndpoint(localUrlHolder.getUrl() + "/xis/idp/userinfo"); // TODO add to controller
        return config;
    }

    @Override
    public IDPTokenResponse provideTokens(IDPTokenRequest request) throws AuthenticationException {
        if (!request.getRedirectUri().startsWith("http")) {
            throw new AuthenticationException("Invalid redirect URI: " + request.getRedirectUri() + ". It must start with 'http(s)'.");
        }
        var clientInfo = idpService.findClientInfo(request.getClientId()).orElseThrow(() -> new AuthenticationException("Client not found: " + request.getClientId()));
        if (!clientInfo.getClientSecret().equals(request.getClientSecret())) {
            throw new AuthenticationException("Invalid client secret for client: " + request.getClientId());
        }
        if (request.getCode() == null || request.getCode().isEmpty()) {
            throw new AuthenticationException("Missing or empty 'code' parameter in the request");
        }
        if (!clientInfo.getPermittedRedirectUrls().contains(request.getRedirectUri())) {
            throw new AuthenticationException("Invalid redirect URI: " + request.getRedirectUri());
        }
        return issueToken(request.getCode());
    }

    @Override
    public JsonWebKey getPublicJsonWebKey() {
        return tokenService.getPublicJsonWebKey();
    }

    /**
     * Generates a new set of access and refresh tokens for the given user ID.
     *
     * @param userId the ID of the user for whom to generate tokens
     * @return an ApiTokens object containing the generated tokens
     * @throws AuthenticationException if the user is not found or token generation fails
     */

    private IDPTokenResponse generateTokenResponse(String userId) throws AuthenticationException {
        IDPUserInfo userInfo = idpService.userInfo(userId).orElseThrow(() -> new AuthenticationException("User not found: " + userId));

        IDPAccessTokenClaims accessTokenClaims = idpService.accessTokenClaims(userId)
                .map(IDPAccessTokenClaims::new)
                .map(claims -> completeTokenClaims(claims, userInfo))
                .orElseThrow(() -> new AuthenticationException("Access token claims not found for user: " + userId));

        IDPIDTokenClaims idTokenClaims = idpService.idTokenClaims(userId)
                .map(IDPIDTokenClaims::new)
                .map(claims -> completeIdTokenClaims(claims, userInfo))
                .orElseThrow(() -> new AuthenticationException("ID token claims not found for user: " + userId));
        RenewTokenClaims renewTokenClaims = idpService.renewTokenClaims(userId);

        String accessToken = tokenService.createToken(accessTokenClaims);
        String idToken = tokenService.createToken(idTokenClaims);
        String refreshToken = tokenService.createToken(renewTokenClaims);

        IDPTokenResponse tokenResponse = new IDPTokenResponse();
        tokenResponse.setAccessToken(accessToken);
        tokenResponse.setIdToken(idToken);
        tokenResponse.setRefreshToken(refreshToken);
        tokenResponse.setExpiresIn(accessTokenClaims.getExpiresAtSeconds());
        tokenResponse.setRefreshExpiresIn(renewTokenClaims.getExpiresAtSeconds());
        return tokenResponse;
    }

    private IDPAccessTokenClaims completeTokenClaims(IDPAccessTokenClaims accessTokenClaims, IDPUserInfo userInfo) {
        accessTokenClaims.setUserId(userInfo.getUserId());
        accessTokenClaims.setIssuedAt(Instant.now().getEpochSecond());
        accessTokenClaims.setExpiresAtSeconds(accessTokenClaims.getIssuedAt() + idpService.getConfig().getAccessTokenValidity().getSeconds());
        accessTokenClaims.setNotBefore(accessTokenClaims.getIssuedAt());
        accessTokenClaims.setClientId(userInfo.getClientId());
        accessTokenClaims.setIssuer(localUrlHolder.getUrl());
        accessTokenClaims.setJwtId(UUID.randomUUID().toString());
        return accessTokenClaims;
    }

    private IDPIDTokenClaims completeIdTokenClaims(IDPIDTokenClaims idTokenClaims, IDPUserInfo userInfo) {
        idTokenClaims.setUserId(userInfo.getUserId());
        idTokenClaims.setIssuedAt(Instant.now().getEpochSecond());
        idTokenClaims.setExpiresAt(idTokenClaims.getIssuedAt() + idpService.getConfig().getIdTokenValidity().getSeconds());
        idTokenClaims.setIssuer(localUrlHolder.getUrl());
        return idTokenClaims;
    }


    /**
     * Verifies the refresh token and extracts the user ID.
     *
     * @param refreshToken the refresh token to verify
     * @return the user ID associated with the refresh token
     * @throws InvalidTokenException if the refresh token is invalid or expired
     */
    private String verifyRefreshToken(String refreshToken) throws InvalidTokenException {
        return tokenService.decodeToken(refreshToken).userId();
    }

}
