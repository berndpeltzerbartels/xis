package one.xis.security;

import one.xis.server.UrlHolder;

public interface AuthenticationConfig {

    UrlHolder getIdpUrl();

    String getClientId();

    String getClientSecret();
}
