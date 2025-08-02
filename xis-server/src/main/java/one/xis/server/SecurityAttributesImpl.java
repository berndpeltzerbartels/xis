package one.xis.server;

import lombok.Data;
import one.xis.auth.token.SecurityAttributes;
import one.xis.auth.token.TokenStatus;
import one.xis.auth.token.UserSecurityService;

import java.util.Set;

@Data
public class SecurityAttributesImpl implements SecurityAttributes {

    private final TokenStatus tokenStatus;
    private final UserSecurityService userSecurityService;
    private String userId;
    private Set<String> roles;
    private boolean completed;

    public String getUserId() {
        if (!completed) {
            load();
            completed = true;
        }
        return userId;
    }

    public Set<String> getRoles() {
        if (!completed) {
            load();
            completed = true;
        }
        return roles;
    }

    private void load() {
        userSecurityService.update(tokenStatus, this);
    }

}
