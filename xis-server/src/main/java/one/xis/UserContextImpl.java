package one.xis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.http.RequestContext;

import java.time.ZoneId;
import java.util.Locale;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserContextImpl implements UserContext {
    
    private Locale locale;
    private ZoneId zoneId;
    private String clientId;


    public static UserContext getInstance() {
        return (UserContext) RequestContext.getInstance().getAttribute(UserContext.CONTEXT_KEY);
    }

}
