package one.xis.auth;

import one.xis.ImportInstances;

import java.util.Optional;

/**
 * Stores hashed credentials for a XIS IDP server.
 * <p>
 * Implement this repository when an IDP application wants to use the default {@link IDPCredentialService} from
 * {@code xis-idp-credentials} but store hashes in its own persistence layer.
 */
@ImportInstances
public interface IDPCredentialRepository {

    /**
     * Finds stored credentials for an IDP user.
     *
     * @param userId IDP user id
     * @return stored user credentials, or empty when no password is configured
     */
    Optional<StoredIDPUserCredentials> findUserById(String userId);

    /**
     * Stores or updates credentials for an IDP user.
     *
     * @param credentials credentials with an encoded password hash
     */
    void saveUser(StoredIDPUserCredentials credentials);

    /**
     * Finds stored credentials for an IDP client.
     *
     * @param clientId IDP client id
     * @return stored client credentials, or empty when no client secret is configured
     */
    Optional<StoredIDPClientCredentials> findClientById(String clientId);

    /**
     * Stores or updates credentials for an IDP client.
     *
     * @param credentials credentials with an encoded client-secret hash
     */
    void saveClient(StoredIDPClientCredentials credentials);
}
