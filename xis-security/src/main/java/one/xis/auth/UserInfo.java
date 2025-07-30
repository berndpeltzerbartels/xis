package one.xis.auth;

import java.util.Set;

public interface UserInfo {
    String getUserId();

    Set<String> getRoles();

}
