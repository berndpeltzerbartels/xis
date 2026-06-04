package one.xis.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stored local password credentials.
 * <p>
 * {@code passwordHash} is the complete encoded Argon2id hash. It contains the algorithm, parameters, salt, and hash
 * value. It is not an encrypted password and must never contain the clear text password.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoredCredentials {

    private String userId;
    private String passwordHash;
}
