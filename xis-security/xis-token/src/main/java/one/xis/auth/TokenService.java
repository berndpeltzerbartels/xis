package one.xis.auth;

import java.time.Duration;

public interface TokenService {
    ApiTokens newTokens(UserInfo userInfo);

    JsonWebKey getPublicJsonWebKey();

    ApiTokens renewTokens(String token, Duration tokenExpiresIn, Duration renewTokenExpiresIn, JsonWebKey jsonWebKey) throws InvalidTokenException;

    AccessTokenClaims decodeAccessToken(String token) throws InvalidTokenException;

    AccessTokenClaims decodeAccessToken(String token, JsonWebKey jwk) throws InvalidTokenException;

    IDTokenClaims decodeIdToken(String token, JsonWebKey jwk) throws InvalidTokenException;

    RenewTokenClaims decodeRenewToken(String token, JsonWebKey jwk) throws InvalidTokenException;

    String createToken(TokenClaims claims);

    String extractKeyId(String token) throws InvalidTokenException;

    String extractIssuer(String token) throws InvalidTokenException;
}
