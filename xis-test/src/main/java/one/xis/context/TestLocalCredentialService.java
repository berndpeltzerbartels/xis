package one.xis.context;

import one.xis.auth.LocalCredentialService;

import java.util.HashMap;
import java.util.Map;

class TestLocalCredentialService implements LocalCredentialService {

    private final Map<String, String> passwords = new HashMap<>();

    @Override
    public boolean validateCredentials(String userId, String password) {
        return passwords.getOrDefault(userId, "").equals(password);
    }

    @Override
    public void setPassword(String userId, String password) {
        passwords.put(userId, password);
    }

    @Override
    public boolean needsRehash(String userId) {
        return false;
    }
}
