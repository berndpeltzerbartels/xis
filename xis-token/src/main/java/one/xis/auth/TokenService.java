package one.xis.auth;

import java.security.KeyPair;

public interface TokenService {
    ApiTokens newTokens(UserInfo userInfo, String issuer, String keyId, KeyPair keyPair);

    AccessTokenClaims decodeAccessToken(String token, JsonWebKey jwk) throws InvalidTokenException;

    IDTokenClaims decodeIdToken(String token, JsonWebKey jwk) throws InvalidTokenException;

    RenewTokenClaims decodeRenewToken(String token, JsonWebKey jwk) throws InvalidTokenException;

    String createToken(TokenClaims claims, String keyId, KeyPair keyPair);

    String extractKeyId(String token) throws InvalidTokenException;

    String extractIssuer(String token) throws InvalidTokenException;

    String extractUserId(String token) throws InvalidTokenException;
    
}
