package one.xis.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Stored IDP client credentials.
 * <p>
 * {@code clientSecretHash} is the complete encoded Argon2id hash. It contains the algorithm, parameters, salt, and hash
 * value. It is not an encrypted client secret and must never contain the clear text secret.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoredIDPClientCredentials {

    private String clientId;
    private String clientSecretHash;
}
