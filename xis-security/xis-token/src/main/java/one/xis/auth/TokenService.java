package one.xis.auth;

import java.time.Duration;

public interface TokenService {
    ApiTokens newTokens(UserInfo userInfo);

    JsonWebKey getPublicJsonWebKey();

    ApiTokens renewTokens(String token, Duration tokenExpiresIn, Duration renewTokenExpiresIn) throws InvalidTokenException;

    AccessTokenClaims decodeAccessToken(String token) throws InvalidTokenException;

    IDTokenClaims decodeIdToken(String token) throws InvalidTokenException;

    RenewTokenClaims decodeRenewToken(String token) throws InvalidTokenException;

    String createToken(TokenClaims claims);

}
