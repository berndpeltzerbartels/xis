package spring;

import jakarta.annotation.PostConstruct;
import one.xis.auth.AccessTokenClaims;
import one.xis.auth.IDTokenClaims;
import one.xis.auth.idp.*;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SpringIDPService implements IDPService {

    // In-Memory-Speicher für Test-Benutzer und -Clients
    private final Map<String, IDPUserInfo> users = new ConcurrentHashMap<>();
    private final Map<String, IDPClientInfo> clients = new ConcurrentHashMap<>();
    private final Map<String, String> passwords = new ConcurrentHashMap<>();

    /**
     * Initialisiert den Service mit Test-Daten.
     */
    @PostConstruct
    public void init() {
        // Beispiel-Benutzer hinzufügen
        var testUser = new IDPUserInfoImpl("user123", "client-app");
        users.put(testUser.getUserId(), testUser);
        passwords.put(testUser.getUserId(), "password123");

        // Beispiel-Client hinzufügen
        var testClient = new IDPClientInfoImpl("client-app", "client-secret", Set.of("http://localhost:8080/xis/auth/callback/test-idp"));
        clients.put(testClient.getClientId(), testClient);
    }

    @Override
    public Optional<IDPUserInfo> userInfo(String userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public Optional<AccessTokenClaims> accessTokenClaims(String userId) {
        IDPUserInfo user = users.get(userId);
        if (user == null) {
            return Optional.empty();
        }
        // Beispiel: AccessTokenClaims mit Username, aber ohne Email
        AccessTokenClaims claims = new AccessTokenClaims();
        claims.setUsername(user.getUserId());
        // weitere Felder nach Bedarf setzen
        return Optional.of(claims);
    }

    @Override
    public Optional<IDTokenClaims> idTokenClaims(String userId) {
        IDPUserInfo user = users.get(userId);
        if (user == null) {
            return Optional.empty();
        }
        IDTokenClaims claims = new IDTokenClaims();
        return Optional.of(claims);
    }

    @Override
    public Optional<IDPClientInfo> findClientInfo(String clientId) {
        return Optional.ofNullable(clients.get(clientId));
    }

    @Override
    public boolean validateCredentials(String username, String password) {
        var userId = Optional.ofNullable(users.get(username))
                .map(IDPUserInfo::getUserId);
        if (userId.isEmpty()) {
            return false;
        }
        return userId.map(users::get)
                .map(IDPUserInfo::getUserId)
                .filter(id -> passwords.get(id).equals(password))
                .isPresent();

    }

    @Override
    public boolean validateClientSecret(String clientId, String clientSecret) {
        return Optional.ofNullable(clients.get(clientId))
                .map(client -> client.getClientSecret().equals(clientSecret))
                .orElse(false);
    }
}