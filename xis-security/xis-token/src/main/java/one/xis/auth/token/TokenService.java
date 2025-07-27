package one.xis.auth.token;

import one.xis.auth.InvalidTokenException;
import one.xis.auth.JsonWebKey;
import one.xis.auth.TokenClaims;
import one.xis.auth.UserInfo;

import java.time.Duration;

public interface TokenService {
    ApiTokens newTokens(UserInfo userInfo);

    JsonWebKey getPublicJsonWebKey();

    TokenAttributes decodeToken(String token) throws InvalidTokenException;

    ApiTokens renewTokens(String token, Duration tokenExpiresIn, Duration renewTokenExpiresIn) throws InvalidTokenException;

    String createToken(TokenClaims claims);

}
