package one.xis.idp;

import lombok.RequiredArgsConstructor;
import one.xis.auth.*;
import one.xis.auth.token.ApiTokens;
import one.xis.auth.token.TokenService;
import one.xis.context.XISComponent;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@XISComponent
@RequiredArgsConstructor
class IDPAuthenticationServiceImpl implements IDPAuthenticationService {

    private final IDPService idpService;
    private final TokenService tokenService;
    private final IDPServerCodeStore idpCodeStore = new IDPServerCodeStore();

    /**
     * Logs in a user using the provided credentials.
     *
     * @param login the login credentials containing username and password
     * @return a unique authorization code for the user session
     * @throws InvalidCredentialsException if the provided credentials are invalid
     */
    @Override
    public String login(IDPServerLogin login) throws InvalidCredentialsException {
        if (!idpService.findUserInfo(login.getUsername())
                .map(IDPUserInfo::getPassword)
                .map(login.getPassword()::equals)
                .orElse(false)) {
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

    @Override
    public ApiTokens issueToken(String code) throws AuthenticationException {
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
    public ApiTokens refresh(String refreshToken) throws InvalidTokenException, AuthenticationException {
        String userId = verifyRefreshToken(refreshToken);
        return generateTokenResponse(userId);
    }


    /**
     * Verifies the provided access token and extracts user information from it.
     *
     * @param accessToken the access token to verify and extract user information
     * @return an IDPUserInfo object containing user details
     */
    @Override
    public IDPUserInfo verifyAndExtractUserInfo(String accessToken) throws InvalidTokenException {
        var attributes = tokenService.decodeToken(accessToken);
        IDPUserInfoImpl userInfo = new IDPUserInfoImpl();
        userInfo.setUserId(attributes.userId());
        userInfo.setRoles(new HashSet<>(attributes.roles()));
        userInfo.setClaims(attributes.claims());
        return userInfo;
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
        if (!idpService.findUserInfo(userId)
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

    /**
     * Generates a new set of access and refresh tokens for the given user ID.
     *
     * @param userId the ID of the user for whom to generate tokens
     * @return an ApiTokens object containing the generated tokens
     * @throws AuthenticationException if the user is not found or token generation fails
     */

    private ApiTokens generateTokenResponse(String userId) throws AuthenticationException {
        UserInfo userInfo = idpService.findUserInfo(userId).orElseThrow(() -> new AuthenticationException("User not found: " + userId));
        return tokenService.newTokens(userInfo);
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
