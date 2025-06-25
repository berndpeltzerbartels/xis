package one.xis.security;

import one.xis.ImportInstances;

@ImportInstances
public interface LocalUserAuthenticator {

    boolean checkCredentials(String userId, String password);

    UserInfo getUserInfo(String userId) throws InvalidTokenException;
}
