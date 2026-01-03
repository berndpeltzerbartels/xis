package one.xis.auth;

import one.xis.context.DefaultComponent;

import java.util.Optional;

@DefaultComponent
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
    public void saveUserInfo(UserInfoImpl userInfo) {

    }
}
