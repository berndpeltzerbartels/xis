package one.xis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Delegate;

import java.time.ZoneId;
import java.util.Locale;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContextImpl implements UserContext {

    static ThreadLocal<UserContextImpl> instance = ThreadLocal.withInitial(UserContextImpl::new);

    private Locale locale;
    private ZoneId zoneId;
    private String clientId;

    @Delegate
    private AccessToken accessToken;


    public static UserContext getInstance() {
        return instance.get();
    }

    static void setInstance(UserContextImpl context) {
        instance.set(context);
    }

    static void removeInstance() {
        instance.remove();
    }

}
