package one.xis.security;

import one.xis.auth.token.ApiTokensAndUrl;

class NoopAuthenticationService implements AuthenticationService {

    @Override
    public String loginUrl(String redirectUri) {
        throw new UnsupportedOperationException("Authentication is not configured. " +
                "Please provide a bean of type AuthenticationConfig " +
                " or implement the IDPService to enable local authentication.");
    }

    @Override
    public ApiTokensAndUrl authenticate(String code, String state) {
        throw new UnsupportedOperationException("Authentication is not configured. " +
                "Please provide a bean of type AuthenticationConfig " +
                " or implement the IDPService to enable local authentication.");
    }
}
