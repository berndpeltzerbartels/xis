package one.xis.auth;

import java.util.Collection;

/**
 * Service for creating and validating JWT tokens. Class is used in case
 * of no external token service is configured.
 */
public interface LocalTokenService {

    /**
     * Returns the JSON Web Keys (JWKs) used for signing and verifying tokens.
     *
     * @return a collection of JSON Web Keys
     */
    Collection<JsonWebKey> getJsonWebKeys();

    /**
     * Generates new API tokens for the specified user ID.
     *
     * @param userId the user ID for which to generate tokens
     * @return the newly generated API tokens
     */
    ApiTokens newTokens(String userId);

    /**
     * Renews API tokens using the provided renew token.
     *
     * @param renewToken
     * @return the renewed API tokens
     * @throws InvalidTokenException if the renew token is invalid
     */
    ApiTokens renewTokens(String renewToken) throws InvalidTokenException;

    /**
     * Decodes the provided renew token and returns its claims.
     *
     * @param token the renew token to decode
     * @return the claims contained in the renew token
     * @throws InvalidTokenException if the token is invalid
     */
    RenewTokenClaims decodeRenewToken(String token) throws InvalidTokenException;

    /**
     * Decodes the provided access token and returns its claims.
     *
     * @param token the access token to decode
     * @return the claims contained in the access token
     * @throws InvalidTokenException if the token is invalid
     */
    AccessTokenClaims decodeAccessToken(String token) throws InvalidTokenException;


    /**
     * Creates a new token based on the provided token claims.
     *
     * @param tokenClaims the claims to include in the token
     * @return the newly created token as a string
     */
    String createToken(TokenClaims tokenClaims);

    /**
     * Decodes the provided ID token and returns its claims.
     *
     * @param idToken the ID token to decode
     * @return the claims contained in the ID token
     */
    IDTokenClaims decodeIdToken(String idToken);
}
