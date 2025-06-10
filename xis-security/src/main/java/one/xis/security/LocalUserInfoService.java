package one.xis.security;

import one.xis.ImportInstances;

@ImportInstances
public interface LocalUserInfoService {

    boolean checkCredentials(String userId, String password);

    LocalUserInfo getUserInfo(String userId) throws AuthenticationException;
}
