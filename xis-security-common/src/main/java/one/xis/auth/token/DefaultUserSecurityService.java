package one.xis.auth.token;

import one.xis.context.XISDefaultComponent;

@XISDefaultComponent
public class DefaultUserSecurityService implements UserSecurityService {

    @Override
    public void update(TokenStatus tokenStatus, SecurityAttributes securityAttributes) {
        // Default implementation does nothing
        // This can be overridden by subclasses if needed
    }
}
