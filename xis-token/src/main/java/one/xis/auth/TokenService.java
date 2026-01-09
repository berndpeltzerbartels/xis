package one.xis.auth;

import java.security.KeyPair;

/**
 * Encapsulates core token operations such as creation, decoding, and extraction of token details
 * independent from use cases.
 *
 */
public interface TokenService {

    /**
     * Creates new API tokens (access and renew tokens) for the given user information.
     *
     * @param userInfo details about the user for whom the tokens are being created
     * @param issuer   the issuer identifier for the tokens
     * @param keyId    the key identifier used for signing the tokens required because we may have multiple keys
     * @param keyPair  the key pair used to sign the tokens
     * @return the newly created API tokens
     */
    ApiTokens newTokens(UserInfo userInfo, String issuer, String keyId, KeyPair keyPair);


    /**
     * Decodes the provided access token using the specified JSON Web Key (JWK).
     *
     * @param token the access token to decode
     * @param jwk   the JSON Web Key used for decoding the token
     * @return the claims contained in the access token
     * @throws InvalidTokenException if the token is invalid
     */
    AccessTokenClaims decodeAccessToken(String token, JsonWebKey jwk) throws InvalidTokenException;

    /**
     * Decodes the provided ID token using the specified JSON Web Key (JWK).
     *
     * @param token the ID token to decode
     * @param jwk   the JSON Web Key used for decoding the token
     * @return the claims contained in the ID token
     * @throws InvalidTokenException if the token is invalid
     */
    IDTokenClaims decodeIdToken(String token, JsonWebKey jwk) throws InvalidTokenException;

    /**
     * Decodes the provided renew token using the specified JSON Web Key (JWK).
     *
     * @param token the renew token to decode
     * @param jwk   the JSON Web Key used for decoding the token
     * @return the claims contained in the renew token
     * @throws InvalidTokenException if the token is invalid
     */
    RenewTokenClaims decodeRenewToken(String token, JsonWebKey jwk) throws InvalidTokenException;

    /**
     * Creates a new token based on the provided token claims, key identifier, and key pair.
     *
     * @param claims  the claims to include in the token
     * @param keyId   the key identifier used for signing the token
     * @param keyPair the key pair used to sign the token
     * @return the newly created token as a string
     */
    String createToken(TokenClaims claims, String keyId, KeyPair keyPair);

    /**
     * Extracts the key identifier from the provided token.
     *
     * @param token the token from which to extract the key identifier
     * @return the extracted key identifier
     * @throws InvalidTokenException if the token is invalid
     */
    String extractKeyId(String token) throws InvalidTokenException;

    /**
     * Extracts the issuer from the provided token.
     *
     * @param token the token from which to extract the issuer
     * @return the extracted issuer
     * @throws InvalidTokenException if the token is invalid
     */
    String extractIssuer(String token) throws InvalidTokenException;


    /**
     * Extracts the user ID from the provided token.
     *
     * @param token the token from which to extract the user ID
     * @return the extracted user ID
     * @throws InvalidTokenException if the token is invalid
     */
    String extractUserId(String token) throws InvalidTokenException;

}
