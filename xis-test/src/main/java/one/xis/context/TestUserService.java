package one.xis.context;

import one.xis.auth.AuthenticationException;
import one.xis.auth.UserInfo;
import one.xis.idp.IDPUserInfo;
import one.xis.security.UserInfoService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TestUserService implements UserInfoService<UserInfo> {
    private final Map<String, UserInfo> users = new HashMap<>();

    public TestUserService(IDPUserInfo... users) {
        Arrays.stream(users).forEach(user -> this.users.put(user.getUserId(), user));
    }

    @Override
    public Optional<UserInfo> getUserInfo(String userId) throws AuthenticationException {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public void saveUserInfo(UserInfo userInfo) {
        users.put(userInfo.getUserId(), userInfo);
    }
}
