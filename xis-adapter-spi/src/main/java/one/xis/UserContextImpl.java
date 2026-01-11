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

    private static final ThreadLocal<UserContext> USER_CONTEXT_THREAD_LOCAL = ThreadLocal.withInitial(UserContextImpl::new);

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
        return securityAttributes.getUserId() != null;
    }

    public static UserContext getInstance() {
        return USER_CONTEXT_THREAD_LOCAL.get();
    }

    public static void clear() {
        USER_CONTEXT_THREAD_LOCAL.remove();
    }

}
