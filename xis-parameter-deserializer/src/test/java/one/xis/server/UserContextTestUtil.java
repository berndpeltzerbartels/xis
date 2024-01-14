package one.xis.server;

import java.time.ZoneId;
import java.util.Locale;

public class UserContextTestUtil {

    public static void setTestContextDe() {
        UserContext.setInstance(new UserContext(Locale.GERMANY, ZoneId.of("Europe/Berlin"), "user", "client"));
    }
}
