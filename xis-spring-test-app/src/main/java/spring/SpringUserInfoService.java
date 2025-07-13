package spring;

import one.xis.auth.InvalidTokenException;
import one.xis.auth.UserInfo;
import one.xis.auth.UserInfoService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringUserInfoService implements UserInfoService<UserInfo> {
    @Override
    public boolean validateCredentials(String userId, String password) {
        return true;
    }

    @Override
    public Optional<UserInfo> getUserInfo(String userId) throws InvalidTokenException {
        return Optional.empty();
    }

    @Override
    public void saveUserInfo(UserInfo userInfo, String password) {
        // No implementation needed for this example

    }
}
