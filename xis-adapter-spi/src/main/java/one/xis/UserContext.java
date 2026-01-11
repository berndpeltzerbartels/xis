package one.xis;

import one.xis.auth.token.SecurityAttributes;
import one.xis.auth.token.TokenStatus;

import java.time.ZoneId;
import java.util.Locale;
import java.util.Set;

public interface UserContext {

    String CONTEXT_KEY = "userContext";

    Locale getLocale();

    ZoneId getZoneId();

    String getClientId();

    String getUserId();

    Set<String> getRoles();


    TokenStatus getTokenStatus();

    boolean isAuthenticated();

    SecurityAttributes getSecurityAttributes();

    static UserContext getInstance() {
        return UserContextImpl.getInstance();
    }

}
