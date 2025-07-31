package one.xis;

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

    boolean isAuthenticated();


}
