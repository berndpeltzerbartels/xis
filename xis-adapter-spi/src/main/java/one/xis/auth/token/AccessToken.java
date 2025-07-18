package one.xis.auth.token;

import java.time.Instant;
import java.util.Collection;

public interface AccessToken {

    String CONTEXT_KEY = "accessToken";

    String getToken();

    boolean isAuthenticated();

    String getUserId();

    Instant getExpiresAt();

    Collection<String> getRoles();

    boolean isExpired();

}
