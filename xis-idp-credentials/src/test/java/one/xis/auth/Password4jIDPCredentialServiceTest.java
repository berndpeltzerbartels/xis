package one.xis.auth;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class Password4jIDPCredentialServiceTest {

    private final InMemoryRepository repository = new InMemoryRepository();
    private final Password4jIDPCredentialService service = new Password4jIDPCredentialService(repository);

    @Test
    void hashesAndValidatesUserPasswords() {
        service.setUserPassword("alice", "secret");

        assertThat(service.validateUserCredentials("alice", "secret")).isTrue();
        assertThat(service.validateUserCredentials("alice", "wrong")).isFalse();
        assertThat(repository.userCredentials.get("alice").getPasswordHash()).startsWith("$argon2id$");
        assertThat(repository.userCredentials.get("alice").getPasswordHash()).doesNotContain("secret");
    }

    @Test
    void hashesAndValidatesClientSecrets() {
        service.setClientSecret("orders-app", "orders-secret");

        assertThat(service.validateClientSecret("orders-app", "orders-secret")).isTrue();
        assertThat(service.validateClientSecret("orders-app", "wrong")).isFalse();
        assertThat(repository.clientCredentials.get("orders-app").getClientSecretHash()).startsWith("$argon2id$");
        assertThat(repository.clientCredentials.get("orders-app").getClientSecretHash()).doesNotContain("orders-secret");
    }

    private static class InMemoryRepository implements IDPCredentialRepository {
        private final Map<String, StoredIDPUserCredentials> userCredentials = new HashMap<>();
        private final Map<String, StoredIDPClientCredentials> clientCredentials = new HashMap<>();

        @Override
        public Optional<StoredIDPUserCredentials> findUserById(String userId) {
            return Optional.ofNullable(userCredentials.get(userId));
        }

        @Override
        public void saveUser(StoredIDPUserCredentials credentials) {
            userCredentials.put(credentials.getUserId(), credentials);
        }

        @Override
        public Optional<StoredIDPClientCredentials> findClientById(String clientId) {
            return Optional.ofNullable(clientCredentials.get(clientId));
        }

        @Override
        public void saveClient(StoredIDPClientCredentials credentials) {
            clientCredentials.put(credentials.getClientId(), credentials);
        }
    }
}
