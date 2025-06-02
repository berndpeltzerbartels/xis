package one.xis.security;

public interface UserService {

    boolean checkCredentials(String userId, String password);

    LocalUserInfo getUserInfo(String userId) throws AuthenticationException;

    void storeLoginCode(String code, String userId);

    String findUserIdForCode(String code);
}
