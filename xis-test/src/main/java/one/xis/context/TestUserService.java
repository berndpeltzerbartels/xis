package one.xis.context;

import one.xis.idp.IDPUserInfo;
import one.xis.security.AuthenticationException;
import one.xis.security.UserInfoService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TestUserService implements UserInfoService<IDPUserInfo> {
    private final Map<String, IDPUserInfo> users = new HashMap<>();

    public TestUserService(IDPUserInfo... users) {
        Arrays.stream(users).forEach(user -> this.users.put(user.getUserId(), user));
    }

    @Override
    public boolean checkCredentials(String userId, String password) {
        IDPUserInfo user = users.get(userId);
        if (user == null) {
            throw new AuthenticationException("User not found: " + userId);
        }
        // For testing, we assume the password is always "password"
        if (!Objects.equals(password, user.getPassword())) {
            throw new AuthenticationException("Invalid credentials for user: " + userId);
        }
        return true;
    }

    @Override
    public IDPUserInfo getUserInfo(String userId) throws AuthenticationException {
        return users.get(userId);
    }

    @Override
    public void saveUserInfo(IDPUserInfo userInfo, String ipdId) {
        users.put(userInfo.getUserId(), userInfo);
    }
}
