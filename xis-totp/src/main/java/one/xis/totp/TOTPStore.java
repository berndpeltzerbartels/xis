package one.xis.totp;

import one.xis.ImportInstances;

import java.util.Optional;
import java.util.OptionalLong;

/**
 * Application-owned storage for TOTP secrets.
 * <p>
 * XIS encrypts newly created secrets before calling {@link #saveEncryptedSecret(String, String)}. Implementations must
 * persist and return exactly that encrypted value; they do not need to know the raw authenticator secret.
 */
@ImportInstances
public interface TOTPStore {

    Optional<String> getEncryptedSecret(String userId);

    void saveEncryptedSecret(String userId, String encryptedSecret);

    default void deleteSecret(String userId) {
    }

    default OptionalLong getLastAcceptedTimeStep(String userId) {
        return OptionalLong.empty();
    }

    default void saveLastAcceptedTimeStep(String userId, long timeStep) {
    }
}
