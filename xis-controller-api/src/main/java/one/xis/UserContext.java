package one.xis;

import java.time.ZoneId;
import java.util.Locale;

public interface UserContext {

    String CONTEXT_KEY = "userContext";

    Locale getLocale();

    ZoneId getZoneId();

    String getClientId();


}
