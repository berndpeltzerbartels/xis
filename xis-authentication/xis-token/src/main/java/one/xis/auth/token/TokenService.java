package one.xis.auth.token;

import one.xis.auth.InvalidTokenException;

import java.time.Duration;

public interface TokenService {
    ApiTokens newTokens(TokenCreationAttributes accessTokenAttributes, TokenCreationAttributes renewTokenAttributes) throws InvalidTokenException;

    TokenAttributes decodeToken(String token) throws InvalidTokenException;

    ApiTokens renewTokens(String token, Duration tokenExpiresIn, Duration renewTokenExpiresIn) throws InvalidTokenException;
}
