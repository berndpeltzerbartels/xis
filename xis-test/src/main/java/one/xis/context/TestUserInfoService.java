package one.xis.context;

import one.xis.auth.UserInfo;
import one.xis.auth.UserInfoImpl;
import one.xis.auth.UserInfoService;

import java.util.*;

@XISDefaultComponent
class TestUserInfoService implements UserInfoService<UserInfo> {

    private final Collection<UserInfo> userInfos = new HashSet<>();
    private final Map<String, String> userPasswords = new HashMap<>();

    void addUserInfo(UserInfo userInfo) {
        userInfos.add(new UserInfoImpl(userInfo.getUserId(), userInfo.getRoles(), userInfo.getClaims()));
    }

    @Override
    public boolean validateCredentials(String userId, String password) {
        return userPasswords.getOrDefault(userId, "").equals(password);
    }

    @Override
    public Optional<UserInfo> getUserInfo(String userId) {
        return userInfos.stream()
                .filter(userInfo -> userInfo.getUserId().equals(userId))
                .findFirst();
    }

    @Override
    public void saveUserInfo(UserInfo userInfo, String password) {
        getUserInfo(userInfo.getUserId()).map(UserInfoImpl.class::cast).ifPresentOrElse(
                existingUserInfo -> {
                    existingUserInfo.setRoles(userInfo.getRoles());
                    existingUserInfo.setClaims(userInfo.getClaims());
                },
                () -> userInfos.add(new UserInfoImpl(userInfo.getUserId(), userInfo.getRoles(), userInfo.getClaims()))
        );
        userPasswords.put(userInfo.getUserId(), password);
    }
}
