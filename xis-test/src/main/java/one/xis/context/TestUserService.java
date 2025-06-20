package one.xis.context;

import one.xis.security.AuthenticationException;
import one.xis.security.LocalUserInfo;
import one.xis.security.LocalUserInfoService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TestUserService implements LocalUserInfoService {
    private final Map<String, LocalUserInfo> users = new HashMap<>();

    public TestUserService(LocalUserInfo... users) {
        Arrays.stream(users).forEach(user -> this.users.put(user.getUserId(), user));
    }

    @Override
    public boolean checkCredentials(String userId, String password) {
        LocalUserInfo user = users.get(userId);
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
    public LocalUserInfo getUserInfo(String userId) throws AuthenticationException {
        return users.get(userId);
    }
}
