package spring;

import lombok.NonNull;
import one.xis.auth.InvalidTokenException;
import one.xis.auth.UserInfo;
import one.xis.auth.UserInfoImpl;
import one.xis.auth.UserInfoService;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public class SpringUserInfoService implements UserInfoService<UserInfo> {

    private final UserInfo userInfo = new UserInfoImpl("admin", Set.of("admin"), Map.of("email", ""));
    private final String password = "bla";

    @Override
    public boolean validateCredentials(@NonNull String userId, @NonNull String password) {
        return userInfo.getUserId().equals(userId) && this.password.equals(password);
    }

    @Override
    public Optional<UserInfo> getUserInfo(@NonNull String userId) throws InvalidTokenException {
        return Optional.ofNullable(userInfo.getUserId().equals(userId) ? userInfo : null);
    }

}
