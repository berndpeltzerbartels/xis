package one.xis.auth;

import java.util.Map;

public interface UserInfo {
    String getUserId();

    String getPassword();

    java.util.Set<String> getRoles();

    Map<String, Object> getClaims();
}
