package one.xis.auth.token;

import one.xis.UserContext;

public interface TokenManager {

    void updateUserContext(TokenStatus tokenStatus, UserContext userContext);


}
