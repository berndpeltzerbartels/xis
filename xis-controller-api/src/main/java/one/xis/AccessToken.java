package one.xis;

import java.time.Instant;
import java.util.Collection;

public interface AccessToken {

    String getToken();

    boolean isAuthenticated();

    String getUserId();

    Instant getExpiresAt();

    Collection<String> getRoles();

    boolean isExpired();

}
