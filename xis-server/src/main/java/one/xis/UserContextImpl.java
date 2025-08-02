package one.xis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.auth.token.SecurityAttributes;
import one.xis.http.RequestContext;

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
        return (UserContext) RequestContext.getInstance().getAttribute(UserContext.CONTEXT_KEY);
    }

}
