package one.xis.security;

import lombok.RequiredArgsConstructor;
import one.xis.server.ApiTokens;

@RequiredArgsConstructor
class LocalAuthenticationImpl implements LocalAuthentication {

    private final LocalUserInfoService userService;
    private final ApiTokenManager tokenManager;

    @Override
    public ApiTokens login(String username, String password) throws AuthenticationException {
        if (userService.checkCredentials(username, password)) {
            var userInfo = userService.getUserInfo(username);
            var tokenResult = tokenManager.createTokens(userInfo.getUserId(), userInfo.getRoles(), userInfo.getClaims());
            return new ApiTokens(
                    tokenResult.accessToken(),
                    tokenResult.accessTokenExpiresIn(),
                    tokenResult.renewToken(),
                    tokenResult.renewTokenExpiresIn()
            );
        } else {
            throw new AuthenticationException();
        }
    }
}
