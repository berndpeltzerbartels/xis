package one.xis.auth;

import java.util.Set;

public interface IDPClientInfo {

    String getClientId();

    Set<String> getPermittedRedirectUrls();

}
