package one.xis.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZoneId;
import java.util.Locale;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContext {

    static ThreadLocal<UserContext> instance = ThreadLocal.withInitial(UserContext::new);

    private Locale locale;
    private ZoneId zoneId;
    private String userId;
    private String clientId;

    public static UserContext getInstance() {
        return instance.get();
    }

    static void setInstance(UserContext context) {
        instance.set(context);
    }

    static void removeInstance() {
        instance.remove();
    }
}
