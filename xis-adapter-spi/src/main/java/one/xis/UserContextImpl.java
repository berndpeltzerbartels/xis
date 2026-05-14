package one.xis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.auth.token.SecurityAttributes;
import one.xis.auth.token.TokenStatus;

import java.time.ZoneId;
import java.util.Locale;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContextImpl implements UserContext {

    private Locale locale;
    private ZoneId zoneId;
    private String clientId;
    private SecurityAttributes securityAttributes;
    private TokenStatus tokenStatus;


    @Override
    public String getUserId() {
        return securityAttributes.getUserId();
    }

    @Override
    public Set<String> getRoles() {
        return securityAttributes.getRoles();
    }

    public boolean isAuthenticated() {
        return securityAttributes != null && securityAttributes.getUserId() != null;
    }

    public static UserContextImpl getInstance() {
        var userContext = UserContextHolder.getInstance();
        if (userContext instanceof UserContextImpl userContextImpl) {
            return userContextImpl;
        }
        var userContextImpl = new UserContextImpl();
        UserContextHolder.setInstance(userContextImpl);
        return userContextImpl;
    }

    public static void clear() {
        UserContextHolder.clear();
    }

}
