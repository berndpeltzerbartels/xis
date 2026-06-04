package one.xis.auth.sql;

import one.xis.auth.LocalCredentialRepository;
import one.xis.auth.StoredCredentials;
import one.xis.context.DefaultComponent;

import java.util.Optional;

@DefaultComponent
public class SqlLocalCredentialRepository implements LocalCredentialRepository {

    private final LocalCredentialsSqlRepository repository;

    SqlLocalCredentialRepository(LocalCredentialsSqlRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<StoredCredentials> findByUserId(String userId) {
        return repository.findByUserId(userId)
                .map(record -> new StoredCredentials(record.getUserId(), record.getPasswordHash()));
    }

    @Override
    public void save(StoredCredentials credentials) {
        repository.save(new LocalCredentialsRecord(credentials.getUserId(), credentials.getPasswordHash()));
    }
}
