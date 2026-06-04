package one.xis.auth;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class Password4jLocalCredentialServiceTest {

    @Test
    void hashesAndVerifiesPassword() {
        var repository = new MemoryCredentialRepository();
        var service = new Password4jLocalCredentialService(repository);

        service.setPassword("alice", "secret");

        assertThat(repository.credentials.get("alice").getPasswordHash()).startsWith("$argon2id$");
        assertThat(repository.credentials.get("alice").getPasswordHash()).doesNotContain("secret");
        assertThat(service.validateCredentials("alice", "secret")).isTrue();
        assertThat(service.validateCredentials("alice", "wrong")).isFalse();
    }

    @Test
    void unknownUserIsInvalid() {
        var service = new Password4jLocalCredentialService(new MemoryCredentialRepository());

        assertThat(service.validateCredentials("alice", "secret")).isFalse();
    }

    private static class MemoryCredentialRepository implements LocalCredentialRepository {
        private final Map<String, StoredCredentials> credentials = new HashMap<>();

        @Override
        public Optional<StoredCredentials> findByUserId(String userId) {
            return Optional.ofNullable(credentials.get(userId));
        }

        @Override
        public void save(StoredCredentials credentials) {
            this.credentials.put(credentials.getUserId(), credentials);
        }
    }
}
