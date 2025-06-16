package one.xis.security;

import java.util.Collection;

public interface IDPUserService {

    Collection<String> getAllowedRedirectUrls();

    boolean checkCredentials(String userId, String password);

    LocalUserInfo getUserInfo(String userId) throws AuthenticationException;
}
