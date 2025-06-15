package one.xis;

import java.time.ZoneId;
import java.util.Locale;

public interface UserContext {


    Locale getLocale();

    String getUserId();

    ZoneId getZoneId();

    String getClientId();
}
