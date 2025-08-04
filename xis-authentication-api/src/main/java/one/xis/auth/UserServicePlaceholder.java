package one.xis.auth;

import one.xis.context.XISDefaultComponent;

import java.util.Optional;

@XISDefaultComponent
public class UserServicePlaceholder implements UserInfoService<UserInfoImpl> {
    @Override
    public boolean validateCredentials(String userId, String password) {
        return false;
    }

    @Override
    public Optional<UserInfoImpl> getUserInfo(String userId) {
        return Optional.empty();
    }

    @Override
    public void saveUserInfo(UserInfo userInfo) {

    }
}
