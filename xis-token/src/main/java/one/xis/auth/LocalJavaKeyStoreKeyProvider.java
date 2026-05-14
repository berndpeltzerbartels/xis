package one.xis.auth;

import one.xis.security.SecurityUtil;

import java.io.FileInputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class LocalJavaKeyStoreKeyProvider implements LocalKeyProvider {
    private final String keystorePath;
    private final String keystorePassword;
    private final String keyPassword;
    private final List<String> keyIds = new ArrayList<>();
    private final Map<String, KeyPair> keyPairMap = new HashMap<>();
    private final AtomicInteger keyIdCounter = new AtomicInteger(-1);

    public LocalJavaKeyStoreKeyProvider(String keystorePath, String keystorePassword, String keyPassword) {
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.keyPassword = keyPassword;
        init();
    }

    private void init() {
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            KeyStore ks = KeyStore.getInstance("JKS");
            ks.load(fis, keystorePassword.toCharArray());
            Enumeration<String> aliases = ks.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                if (ks.isKeyEntry(alias)) {
                    PrivateKey priv = (PrivateKey) ks.getKey(alias, keyPassword.toCharArray());
                    Certificate cert = ks.getCertificate(alias);
                    PublicKey pub = cert.getPublicKey();
                    keyPairMap.put(alias, new KeyPair(pub, priv));
                    keyIds.add(alias);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Fehler beim Laden des Keystores", e);
        }
    }

    private KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        return gen.generateKeyPair();
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
        var ids = keyIds;
        int size = ids.size();
        if (size == 0) throw new IllegalStateException("Keine Keys vorhanden");
        int index = keyIdCounter.updateAndGet(i -> (i + 1) % size);
        return ids.get(index);
    }

    public static String generateRandomPassword() {
        return SecurityUtil.createRandomKey(32);
    }
}