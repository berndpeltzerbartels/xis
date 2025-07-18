package one.xis.auth;

import java.util.Map;
import java.util.Set;

public interface UserInfo {
    String getUserId();

    Set<String> getRoles();

    Map<String, Object> getClaims();
}
