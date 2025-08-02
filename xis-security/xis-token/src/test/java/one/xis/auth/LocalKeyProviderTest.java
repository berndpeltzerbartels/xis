package one.xis.auth;

import org.junit.jupiter.api.Test;

class LocalKeyProviderTest {

    @Test
    void createJsonWebKey() {
        LocalInMemoryKeyProvider provider = new LocalInMemoryKeyProvider();
        provider.init();
        String keyId = provider.getKeyIds().iterator().next();

        long start = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            provider.getJsonWebKey(keyId);
        }
        long end = System.nanoTime();
        System.out.println("Dauer für 10000 Aufrufe: " + ((end - start) / 1_000_000.0) + " ms");
    }
}