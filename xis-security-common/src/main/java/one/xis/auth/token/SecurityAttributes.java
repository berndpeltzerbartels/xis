package one.xis.auth.token;

import java.util.Set;

public interface SecurityAttributes {

    String getUserId();

    Set<String> getRoles();

    void setUserId(String userId);

    void setRoles(Set<String> roles);
}

