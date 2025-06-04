package one.xis.security;

public interface UserService {

    boolean checkCredentials(String userId, String password);

    LocalUserInfo getUserInfo(String userId) throws AuthenticationException;
}
