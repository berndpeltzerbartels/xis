package one.xis.auth;

import com.password4j.Argon2Function;
import com.password4j.Password;
import com.password4j.types.Argon2;
import one.xis.context.DefaultComponent;

/**
 * Default {@link IDPCredentialService} backed by Password4j Argon2id hashes.
 */
@DefaultComponent
public class Password4jIDPCredentialService implements IDPCredentialService {

    private static final Argon2Function ARGON2ID = Argon2Function.getInstance(15360, 2, 1, 32, Argon2.ID);

    private final IDPCredentialRepository repository;

    public Password4jIDPCredentialService(IDPCredentialRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean validateUserCredentials(String userId, String password) {
        if (userId == null || password == null) {
            return false;
        }
        return repository.findUserById(userId)
                .map(StoredIDPUserCredentials::getPasswordHash)
                .filter(hash -> !hash.isBlank())
                .filter(hash -> Password.check(password, hash).with(ARGON2ID))
                .isPresent();
    }

    @Override
    public void setUserPassword(String userId, String password) {
        validateId(userId, "userId");
        validateSecret(password, "password");
        repository.saveUser(new StoredIDPUserCredentials(userId, hash(password)));
    }

    @Override
    public boolean validateClientSecret(String clientId, String clientSecret) {
        if (clientId == null || clientSecret == null) {
            return false;
        }
        return repository.findClientById(clientId)
                .map(StoredIDPClientCredentials::getClientSecretHash)
                .filter(hash -> !hash.isBlank())
                .filter(hash -> Password.check(clientSecret, hash).with(ARGON2ID))
                .isPresent();
    }

    @Override
    public void setClientSecret(String clientId, String clientSecret) {
        validateId(clientId, "clientId");
        validateSecret(clientSecret, "clientSecret");
        repository.saveClient(new StoredIDPClientCredentials(clientId, hash(clientSecret)));
    }

    @Override
    public boolean userPasswordNeedsRehash(String userId) {
        if (userId == null) {
            return false;
        }
        return repository.findUserById(userId)
                .map(StoredIDPUserCredentials::getPasswordHash)
                .filter(hash -> !hash.isBlank())
                .map(this::usesCurrentParameters)
                .map(current -> !current)
                .orElse(false);
    }

    @Override
    public boolean clientSecretNeedsRehash(String clientId) {
        if (clientId == null) {
            return false;
        }
        return repository.findClientById(clientId)
                .map(StoredIDPClientCredentials::getClientSecretHash)
                .filter(hash -> !hash.isBlank())
                .map(this::usesCurrentParameters)
                .map(current -> !current)
                .orElse(false);
    }

    private void validateId(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }

    private void validateSecret(String value, String name) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty");
        }
    }

    private String hash(String secret) {
        return Password.hash(secret).addRandomSalt().with(ARGON2ID).getResult();
    }

    private boolean usesCurrentParameters(String passwordHash) {
        try {
            return ARGON2ID.equals(Argon2Function.getInstanceFromHash(passwordHash));
        } catch (RuntimeException e) {
            return false;
        }
    }
}
