package one.xis.idp;

import one.xis.security.AuthenticationException;

import java.util.Collection;

public interface LocalIDPUserService {

    Collection<String> getAllowedRedirectUrls();

    boolean checkCredentials(String userId, String password);

    UserInfo getUserInfo(String userId) throws AuthenticationException;
}
