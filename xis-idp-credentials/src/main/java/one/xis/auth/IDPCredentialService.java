package one.xis.auth;

import one.xis.ImportInstances;

/**
 * Validates and manages credentials owned by a XIS IDP server.
 * <p>
 * User passwords authenticate users at the IDP login form. Client secrets authenticate registered OpenID Connect
 * clients at the token endpoint. Implementations must store both as password hashes, not as clear text secrets.
 */
@ImportInstances
public interface IDPCredentialService {

    /**
     * Validates the submitted password for an IDP user.
     *
     * @param userId   IDP user id
     * @param password submitted password
     * @return true when the password is accepted
     */
    boolean validateUserCredentials(String userId, String password);

    /**
     * Hashes and stores a new password for an IDP user.
     *
     * @param userId   IDP user id
     * @param password new password
     */
    void setUserPassword(String userId, String password);

    /**
     * Validates the submitted client secret for an IDP client.
     *
     * @param clientId     IDP client id
     * @param clientSecret submitted client secret
     * @return true when the secret is accepted
     */
    boolean validateClientSecret(String clientId, String clientSecret);

    /**
     * Hashes and stores a new secret for an IDP client.
     *
     * @param clientId     IDP client id
     * @param clientSecret new client secret
     */
    void setClientSecret(String clientId, String clientSecret);

    /**
     * Returns whether the stored user password hash should be replaced after the next successful login.
     *
     * @param userId IDP user id
     * @return true when the current implementation would store a stronger or newer hash
     */
    boolean userPasswordNeedsRehash(String userId);

    /**
     * Returns whether the stored client secret hash should be replaced after the next successful client authentication.
     *
     * @param clientId IDP client id
     * @return true when the current implementation would store a stronger or newer hash
     */
    boolean clientSecretNeedsRehash(String clientId);
}
