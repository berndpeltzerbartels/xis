package spring;

import lombok.NonNull;
import one.xis.auth.InvalidTokenException;
import one.xis.auth.UserInfoImpl;
import one.xis.auth.UserInfoService;

import java.util.Optional;

//@Component
public class SpringUserInfoService implements UserInfoService<UserInfoImpl> {

    private final UserInfoImpl userInfo = new UserInfoImpl();

    {
        userInfo.setUserId("testUser");
    }

    private final String password = "bla";

    @Override
    public boolean validateCredentials(@NonNull String userId, @NonNull String password) {
        return userInfo.getUserId().equals(userId) && this.password.equals(password);
    }

    @Override
    public Optional<UserInfoImpl> getUserInfo(@NonNull String userId) throws InvalidTokenException {
        if (userId.equals(userInfo.getUserId())) {
            return Optional.of(userInfo);
        }
        return Optional.empty();
    }

    @Override
    public void saveUserInfo(UserInfoImpl userInfo) {

    }

}
