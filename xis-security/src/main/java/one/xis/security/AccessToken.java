package one.xis.security;

import java.time.Instant;
import java.util.Collection;

public interface AccessToken {

    String getToken();

    boolean isAuthenticated();

    String getUserId();

    Instant getExpiresAt();

    Collection<String> getRoles();

    boolean isExpired();

    static AccessToken create(String accessToken, ApiTokenManager tokenManager) {
        return new AccessTokenWrapper(accessToken, tokenManager);
    }

}
