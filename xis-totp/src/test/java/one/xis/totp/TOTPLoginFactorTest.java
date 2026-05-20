package one.xis.totp;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

import static org.assertj.core.api.Assertions.assertThat;

class TOTPLoginFactorTest {

    @AfterEach
    void clearProperties() {
        System.clearProperty("xis.totp.encryption-key");
        System.clearProperty("xis.totp.registration-url");
    }

    @Test
    void requiresAndVerifiesStoredSecret() {
        System.setProperty("xis.totp.encryption-key", "test-key");
        MemoryStore store = new MemoryStore();
        TOTPConfig config = new TOTPConfig();
        TOTPSecretEncryption encryption = new TOTPSecretEncryption(config);
        TOTPGenerator generator = new TOTPGenerator();
        generator.setClock(Clock.fixed(Instant.ofEpochSecond(59), ZoneOffset.UTC));
        store.saveEncryptedSecret("mara", encryption.encrypt("GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ"));
        TOTPLoginFactor factor = new TOTPLoginFactor(store, encryption, generator, config);

        assertThat(factor.isRequired("mara")).isTrue();
        assertThat(factor.verify("mara", "287082")).isTrue();
        assertThat(factor.verify("mara", "287082")).isFalse();
        assertThat(factor.verify("mara", "000000")).isFalse();
    }

    @Test
    void userWithoutStoredSecretDoesNotRequireFactor() {
        TOTPLoginFactor factor = new TOTPLoginFactor(new MemoryStore(), null, null, new TOTPConfig());

        assertThat(factor.isRequired("mara")).isFalse();
    }

    @Test
    void exposesRegistrationLink() {
        System.setProperty("xis.totp.registration-url", "/security/totp.html");
        TOTPLoginFactor factor = new TOTPLoginFactor(new MemoryStore(), null, null, new TOTPConfig());

        assertThat(factor.registration())
                .hasValueSatisfying(registration -> {
                    assertThat(registration.url()).isEqualTo("/security/totp.html");
                    assertThat(registration.messageKey()).isEqualTo("totp.registration.link");
                });
    }

    private static class MemoryStore implements TOTPStore {
        private final Map<String, String> encryptedSecrets = new HashMap<>();
        private final Map<String, Long> acceptedSteps = new HashMap<>();

        @Override
        public Optional<String> getEncryptedSecret(String userId) {
            return Optional.ofNullable(encryptedSecrets.get(userId));
        }

        @Override
        public void saveEncryptedSecret(String userId, String encryptedSecret) {
            encryptedSecrets.put(userId, encryptedSecret);
        }

        @Override
        public OptionalLong getLastAcceptedTimeStep(String userId) {
            Long step = acceptedSteps.get(userId);
            return step == null ? OptionalLong.empty() : OptionalLong.of(step);
        }

        @Override
        public void saveLastAcceptedTimeStep(String userId, long timeStep) {
            acceptedSteps.put(userId, timeStep);
        }
    }
}
