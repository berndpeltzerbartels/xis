package one.xis.totp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class TOTPProvisioningServiceTest {

    @AfterEach
    void clearProperties() {
        System.clearProperty("xis.totp.encryption-key");
        System.clearProperty("xis.totp.issuer");
    }

    @Test
    void storesEncryptedSecretAndBuildsProvisioningUri() {
        System.setProperty("xis.totp.encryption-key", "test-key");
        System.setProperty("xis.totp.issuer", "Test App");
        MemoryStore store = new MemoryStore();
        TOTPConfig config = new TOTPConfig();
        TOTPSecretEncryption encryption = new TOTPSecretEncryption(config);
        TOTPGenerator generator = new TOTPGenerator();
        TOTPProvisioningService service = new TOTPProvisioningService(store, encryption, generator, config);

        String uri = service.provisioningUri("mara");

        assertThat(uri).startsWith("otpauth://totp/Test%20App%3Amara?secret=");
        assertThat(uri).contains("&issuer=Test%20App");
        assertThat(store.encryptedSecrets.get("mara")).isNotBlank();
        assertThat(store.encryptedSecrets.get("mara")).doesNotContain("otpauth");
        assertThat(encryption.decrypt(store.encryptedSecrets.get("mara"))).isEqualTo(service.getOrCreateSecret("mara"));
    }

    private static class MemoryStore implements TOTPStore {
        private final Map<String, String> encryptedSecrets = new HashMap<>();

        @Override
        public Optional<String> getEncryptedSecret(String userId) {
            return Optional.ofNullable(encryptedSecrets.get(userId));
        }

        @Override
        public void saveEncryptedSecret(String userId, String encryptedSecret) {
            encryptedSecrets.put(userId, encryptedSecret);
        }
    }
}
