package one.xis.auth.idp;

import java.util.Set;

public interface IDPClientInfo {

    String getClientId();

    String getClientSecret();

    Set<String> getPermittedRedirectUrls();

}
