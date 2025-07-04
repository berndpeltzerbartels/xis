package one.xis.auth.token;

import one.xis.auth.InvalidTokenException;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;

public interface TokenService {
    ApiTokens newTokens(String userId, Collection<String> roles, Map<String, Object> claims);

    ApiTokens newTokens(TokenCreationAttributes accessTokenAttributes, TokenCreationAttributes renewTokenAttributes);

    TokenAttributes decodeToken(String token) throws InvalidTokenException;

    ApiTokens renewTokens(String token, Duration tokenExpiresIn, Duration renewTokenExpiresIn) throws InvalidTokenException;
}
