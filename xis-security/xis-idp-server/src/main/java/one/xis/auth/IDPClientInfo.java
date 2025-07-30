package one.xis.auth;

import java.util.Set;

public interface IDPClientInfo {

    String getClientId();

    String getClientSecret();

    Set<String> getPermittedRedirectUrls();

}
