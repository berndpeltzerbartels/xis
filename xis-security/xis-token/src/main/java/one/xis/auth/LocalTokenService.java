package one.xis.auth;

import java.util.Collection;

public interface LocalTokenService {

    Collection<JsonWebKey> getJsonWebKeys();

    ApiTokens newTokens(String userId);

    ApiTokens renewTokens(String renewToken) throws InvalidTokenException;

    RenewTokenClaims decodeRenewToken(String token) throws InvalidTokenException;

    AccessTokenClaims decodeAccessToken(String token) throws InvalidTokenException;

    String createToken(TokenClaims tokenClaims);

    IDTokenClaims decodeIdToken(String idToken);
}
