package one.xis.security;

import one.xis.auth.token.ApiTokensAndUrl;

public interface AuthenticationService {
    String loginUrl(String redirectUri);

    ApiTokensAndUrl authenticationCallback(String query);
}
