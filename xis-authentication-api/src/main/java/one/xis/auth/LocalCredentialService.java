package one.xis.auth;

import one.xis.ImportInstances;

/**
 * Validates and manages local username/password credentials.
 * <p>
 * Applications normally get the default implementation from {@code xis-local-credentials}. Replace this service only
 * when credentials are checked by a different system such as LDAP or by a legacy password store.
 */
@ImportInstances
public interface LocalCredentialService {

    /**
     * Validates the submitted password for the local user id.
     *
     * @param userId   local user id
     * @param password submitted password
     * @return true when the password is accepted
     */
    boolean validateCredentials(String userId, String password);

    /**
     * Hashes and stores a new password for the local user id.
     *
     * @param userId   local user id
     * @param password new password
     */
    void setPassword(String userId, String password);

    /**
     * Returns whether the stored password hash should be replaced after the next successful login.
     *
     * @param userId local user id
     * @return true when the stored hash uses outdated parameters
     */
    boolean needsRehash(String userId);
}
