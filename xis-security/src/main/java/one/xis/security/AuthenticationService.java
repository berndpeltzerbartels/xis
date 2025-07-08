package one.xis.security;

import one.xis.auth.token.ApiTokensAndUrl;

public interface AuthenticationService {

    String loginUrl(String redirectUri);

    ApiTokensAndUrl authenticate(String code, String state);

}
