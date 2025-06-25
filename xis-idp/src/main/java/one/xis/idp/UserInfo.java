package one.xis.idp;

import java.util.Map;

public interface UserInfo {
    String getUserId();

    String getPassword();

    java.util.Set<String> getRoles();

    Map<String, Object> getClaims();
}
