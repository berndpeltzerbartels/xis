// LocalKubernetesKeyProvider.java
package one.xis.auth;

import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalKubernetesKeyProvider implements LocalKeyProvider {
    private final String secretMountPath;
    private final List<String> keyIds = new ArrayList<>();
    private final Map<String, KeyPair> keyPairMap = new HashMap<>();
    private final AtomicInteger keyIdCounter = new AtomicInteger(-1);

    public LocalKubernetesKeyProvider(String secretMountPath) {
        this.secretMountPath = secretMountPath;
        init();
    }

    private void init() {
        File dir = new File(secretMountPath);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalStateException("Secret mount path not found: " + secretMountPath);
        }
        // Erwartet: je Key ein <keyId>.pub (PublicKey) und <keyId>.key (PrivateKey)
        List<String> ids = Arrays.stream(Objects.requireNonNull(dir.listFiles()))
                .map(f -> f.getName().split("\\.")[0])
                .distinct().toList();
        for (String keyId : ids) {
            try {
                byte[] pubBytes = Files.readAllBytes(new File(dir, keyId + ".pub").toPath());
                byte[] privBytes = Files.readAllBytes(new File(dir, keyId + ".key").toPath());
                KeyFactory kf = KeyFactory.getInstance("RSA");
                PublicKey pub = kf.generatePublic(new X509EncodedKeySpec(pubBytes));
                PrivateKey priv = kf.generatePrivate(new PKCS8EncodedKeySpec(privBytes));
                keyPairMap.put(keyId, new KeyPair(pub, priv));
                keyIds.add(keyId);
            } catch (Exception e) {
                throw new IllegalStateException("Error loading key " + keyId, e);
            }
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

    public String nextKeyId() {
        return LocalKeyProvider.super.nextKeyId(keyIds, keyIdCounter);
    }
}