package one.xis.auth.idp;

import one.xis.ImportInstances;
import one.xis.auth.AccessTokenClaims;
import one.xis.auth.IDTokenClaims;
import one.xis.auth.RenewTokenClaims;

import java.time.Instant;
import java.util.Optional;

/**
 * IDPService is the interface for the Identity Provider (IDP) service.
 * It provides methods to retrieve access token claims, ID token claims,
 * client information, and validate credentials and client secrets.
 * This service is used to manage user authentication and authorization
 * in the XIS platform in case XIS is used as an IDP.
 */
@ImportInstances
public interface IDPService {

    /**
     * Returns the user information for the given user ID.
     * The user information includes the user's ID, roles, and other claims.
     * This method is used to retrieve the user's information from the IDP.
     *
     * @param userId The ID of the user.
     * @return An Optional containing the user information if found, otherwise empty.
     */
    Optional<IDPUserInfo> userInfo(String userId);

    /**
     * Returns the payload of the access token for the given user ID.
     * The access token is a JWT that contains the user's ID, roles, and other claims.
     * This method is used to retrieve the claims from the access token without decoding it.
     * It is useful for checking the user's roles and other claims without needing to decode the JWT.
     *
     * @param userId
     * @return
     */
    Optional<AccessTokenClaims> accessTokenClaims(String userId);

    /**
     * Returns the payload of the ID token for the given user ID.
     * The ID token is a JWT that contains the user's ID, roles, and other claims.
     * This method is used to retrieve the claims from the ID token without decoding it.
     * It is useful for checking the user's identity and other claims without needing to decode the JWT.
     *
     * @param userId
     * @return
     */
    Optional<IDTokenClaims> idTokenClaims(String userId);

    /**
     * Creates a new RenewTokenClaims object for the given user ID.
     * This method is used to create a claims object that can be used to renew the access token.
     * It checks if the user exists and throws an exception if the user is not found.
     *
     * @param userId The ID of the user for whom to create the RenewTokenClaims.
     * @return A new RenewTokenClaims object for the user.
     * @throws IllegalArgumentException if the user does not exist.
     */
    default RenewTokenClaims renewTokenClaims(String userId) {
        if (userInfo(userId).isEmpty()) {
            throw new IllegalArgumentException("User not found: " + userId);
        }
        var expiresAt = Instant.now().getEpochSecond() + getConfig().getRefreshTokenValidity().getSeconds();
        return new RenewTokenClaims(userId, expiresAt);
    }

    /**
     * Finds the client information for the given client ID. This is to check if the client is registered
     * e.g. when the client demands the tokens from the IDP.
     */
    Optional<IDPClientInfo> findClientInfo(String clientId);

    /**
     * Validates the provided credentials (username and password).
     * This method is used to authenticate users against the IDP.
     *
     * @param username The username of the user.
     * @param password The password of the user.
     * @return true if the credentials are valid, false otherwise.
     */
    boolean validateCredentials(String username, String password);

    /**
     * Validates the client secret for the given client ID.
     * This method is used to authenticate clients against the IDP.
     *
     * @param clientId     The client ID of the application.
     * @param clientSecret The client secret of the application.
     * @return true if the client secret is valid, false otherwise.
     */
    boolean validateClientSecret(String clientId, String clientSecret);


    /**
     * Returns the configuration for the IDP service.
     * This method is used to retrieve the IDP configuration, such as the issuer URL,
     * supported authentication methods, and other settings.
     *
     * @return The IDP configuration.
     */
    default IDPConfig getConfig() {
        return new DefaultIDPConfig();
    }

}
