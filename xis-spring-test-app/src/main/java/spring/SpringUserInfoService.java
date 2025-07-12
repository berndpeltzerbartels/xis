package spring;

import one.xis.auth.InvalidTokenException;
import one.xis.auth.UserInfo;
import one.xis.security.UserInfoService;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringUserInfoService implements UserInfoService<UserInfo> {
    @Override
    public Optional<UserInfo> getUserInfo(String userId) throws InvalidTokenException {
        return Optional.empty();
    }

    @Override
    public void saveUserInfo(UserInfo userInfo) {

    }
}
