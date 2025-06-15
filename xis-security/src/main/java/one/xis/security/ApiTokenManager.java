package one.xis.security;

import java.util.Collection;
import java.util.Map;

public interface ApiTokenManager {

    TokenResult createTokens(TokenRequest tokenRequest) throws InvalidTokenException;

    TokenResult createTokens(String userId, Collection<String> roles, Map<String, Object> claims);

    TokenAttributes decodeToken(String token) throws InvalidTokenException;

    TokenResult renew(String token) throws InvalidTokenException;
}
