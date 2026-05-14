package one.xis;

import java.time.ZoneId;
import java.util.Locale;
import java.util.Set;

/**
 * Provides request-local information about the current browser client and, when authentication is active, the current
 * user.
 */
public interface UserContext {

    String CONTEXT_KEY = "userContext";

    Locale getLocale();

    ZoneId getZoneId();

    String getClientId();

    String getUserId();

    Set<String> getRoles();

    boolean isAuthenticated();

    static UserContext getInstance() {
        return UserContextHolder.getInstance();
    }

    static void clear() {
        UserContextHolder.clear();
    }
}
