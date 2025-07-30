package one.xis.auth;

import one.xis.context.XISDefaultComponent;

import java.util.Optional;

@XISDefaultComponent
public class UserServicePlaceholder implements UserInfoService<UserInfo> {
    @Override
    public boolean validateCredentials(String userId, String password) {
        return false;
    }

    @Override
    public Optional<UserInfo> getUserInfo(String userId) {
        return Optional.empty();
    }
}
