package one.xis.idp;

import java.util.Set;

public interface IDPClientInfo {

    String getClientId();

    String getClientSecret();

    Set<String> getPermittedRedirectUrls();

}
