package one.xis.auth.ipdclient;

import lombok.RequiredArgsConstructor;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;
import java.util.Optional;

/**
 * Encapsulates the public keys (JWKS) of a single Identity Provider.
 * The keys are stored in a map with their Key ID (kid) as the key.
 */
@RequiredArgsConstructor
public class IDPPublicKeys {

    private final Map<String, RSAPublicKey> keysByKid;

    /**
     * Finds a public key by its Key ID (kid).
     *
     * @param kid The Key ID of the requested key.
     * @return An Optional containing the PublicKey if found, otherwise an empty Optional.
     */
    public Optional<PublicKey> getKey(String kid) {
        return Optional.ofNullable(keysByKid.get(kid));
    }
}