package one.xis.auth;

import one.xis.ImportInstances;

import java.util.Optional;

/**
 * Stores local password credential records.
 * <p>
 * Implement this repository when an application wants to use the default {@link LocalCredentialService} from
 * {@code xis-local-credentials} but store credentials in its own persistence layer.
 */
@ImportInstances
public interface LocalCredentialRepository {

    /**
     * Finds the stored credential record for a local user id.
     *
     * @param userId local user id
     * @return stored credentials, or empty when the user has no local password
     */
    Optional<StoredCredentials> findByUserId(String userId);

    /**
     * Stores or updates credentials for a local user id.
     *
     * @param credentials credentials with an encoded password hash
     */
    void save(StoredCredentials credentials);
}
