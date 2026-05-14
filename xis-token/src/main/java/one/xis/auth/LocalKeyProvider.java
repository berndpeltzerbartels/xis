package one.xis.auth;

import one.xis.ImportInstances;

import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@ImportInstances
public interface LocalKeyProvider {

    KeyPair getKeyPair(String keyId);

    default JsonWebKey getJsonWebKey(String keyId) {
        KeyPair keyPair = getKeyPair(keyId);
        if (keyPair == null) return null;
        return createJsonWebKey(keyId, (RSAPublicKey) keyPair.getPublic());
    }

    Collection<String> getKeyIds();

    default Collection<JsonWebKey> getJsonWebKeys() {
        return getKeyIds().stream()
                .map(this::getJsonWebKey)
                .toList();
    }

    default String nextKeyId(List<String> keyIds, AtomicInteger keyIdCounter) {
        if (keyIds.isEmpty()) throw new IllegalStateException("No keys available");
        int index = keyIdCounter.updateAndGet(i -> (i + 1) % keyIds.size());
        return keyIds.get(index);
    }

    default JsonWebKey createJsonWebKey(String keyId, RSAPublicKey publicKey) {
        JsonWebKey jwk = new JsonWebKey();
        jwk.setKeyType("RSA");
        jwk.setAlgorithm("RS256");
        jwk.setPublicKeyUse("sig"); // "sig" für Signatur-Verwendung
        jwk.setKeyId(keyId); // Eindeutige Key-ID

        // Modulus und Exponent müssen Base64URL-kodiert sein.
        String n = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getModulus().toByteArray());
        String e = Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getPublicExponent().toByteArray());

        jwk.setRsaModulus(n);
        jwk.setRsaExponent(e);

        return jwk;
    }

}