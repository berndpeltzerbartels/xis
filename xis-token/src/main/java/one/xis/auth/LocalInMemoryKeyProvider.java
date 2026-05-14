// LocalInMemoryKeyProvider.java
package one.xis.auth;

import one.xis.context.DefaultComponent;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@DefaultComponent
public class LocalInMemoryKeyProvider implements LocalKeyProvider {
    private final Map<String, KeyPair> keyPairMap = new HashMap<>();
    private final List<String> keyIds = new ArrayList<>();

    public LocalInMemoryKeyProvider() {
        init();
    }

    private void init() {
        for (int i = 0; i < 3; i++) {
            String keyId = "key" + i;
            KeyPair keyPair = generateRsaKeyPair();
            keyPairMap.put(keyId, keyPair);
            keyIds.add(keyId);
        }
    }

    @Override
    public KeyPair getKeyPair(String keyId) {
        return keyPairMap.get(keyId);
    }

    @Override
    public Collection<String> getKeyIds() {
        return Collections.unmodifiableList(keyIds);
    }

    private KeyPair generateRsaKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Could not generate RSA key pair", e);
        }
    }
}