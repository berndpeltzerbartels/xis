package one.xis.auth;

import lombok.RequiredArgsConstructor;
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

        AccessTokenClaims accessTokenClaims = idpService.accessTokenClaims(userId)
                .map(claims -> completeTokenClaims(claims, userInfo))
                .orElseThrow(() -> new AuthenticationException("Access token claims not found for user: " + userId));

        IDTokenClaims idTokenClaims = idpService.idTokenClaims(userId)
                .map(claims -> completeTokenClaims(claims, userInfo))
                .orElseThrow(() -> new AuthenticationException("ID token claims not found for user: " + userId));
        RenewTokenClaims renewTokenClaims = completeTokenClaims(idpService.renewTokenClaims(userId), userInfo);

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

    private <C extends TokenClaims> C completeTokenClaims(C tokenClaims, IDPUserInfo userInfo) {
        tokenClaims.setUserId(userInfo.getUserId());
        tokenClaims.setIssuedAtSeconds(Instant.now().getEpochSecond());
        tokenClaims.setExpiresAtSeconds(tokenClaims.getIssuedAtSeconds() + idpService.getConfig().getAccessTokenValidity().getSeconds());
        tokenClaims.setNotBeforeSeconds(tokenClaims.getIssuedAtSeconds());
        tokenClaims.setClientId(userInfo.getClientId());
        tokenClaims.setIssuer(localUrlHolder.getUrl());
        return tokenClaims;
    }
    

}
