package one.xis.auth;

import com.password4j.Argon2Function;
import com.password4j.Password;
import com.password4j.types.Argon2;
import one.xis.context.DefaultComponent;

/**
 * Default {@link LocalCredentialService} backed by Password4j Argon2id hashes.
 */
@DefaultComponent
public class Password4jLocalCredentialService implements LocalCredentialService {

    private static final Argon2Function ARGON2ID = Argon2Function.getInstance(15360, 2, 1, 32, Argon2.ID);

    private final LocalCredentialRepository repository;

    public Password4jLocalCredentialService(LocalCredentialRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean validateCredentials(String userId, String password) {
        if (userId == null || password == null) {
            return false;
        }
        return repository.findByUserId(userId)
                .map(StoredCredentials::getPasswordHash)
                .filter(hash -> !hash.isBlank())
                .filter(hash -> Password.check(password, hash).with(ARGON2ID))
                .isPresent();
    }

    @Override
    public void setPassword(String userId, String password) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("password must not be empty");
        }
        repository.save(new StoredCredentials(userId, hash(password)));
    }

    @Override
    public boolean needsRehash(String userId) {
        if (userId == null) {
            return false;
        }
        return repository.findByUserId(userId)
                .map(StoredCredentials::getPasswordHash)
                .filter(hash -> !hash.isBlank())
                .map(this::usesCurrentParameters)
                .map(current -> !current)
                .orElse(false);
    }

    private String hash(String password) {
        return Password.hash(password).addRandomSalt().with(ARGON2ID).getResult();
    }

    private boolean usesCurrentParameters(String passwordHash) {
        try {
            return ARGON2ID.equals(Argon2Function.getInstanceFromHash(passwordHash));
        } catch (RuntimeException e) {
            return false;
        }
    }
}
