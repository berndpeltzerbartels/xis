package one.xis.security;


import one.xis.server.ApiTokens;

import java.util.Collection;
import java.util.Map;

public interface IDPClientService {

    ApiTokens requestTokens(String code, String state) throws InvalidTokenException;

    TokenResult createTokens(String userId, Collection<String> roles, Map<String, Object> claims);

    TokenAttributes decodeToken(String token) throws InvalidTokenException;

    TokenResult renew(String token) throws InvalidTokenException;
}
