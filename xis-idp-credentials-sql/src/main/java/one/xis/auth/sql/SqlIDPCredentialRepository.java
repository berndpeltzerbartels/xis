package one.xis.auth.sql;

import one.xis.auth.IDPCredentialRepository;
import one.xis.auth.StoredIDPClientCredentials;
import one.xis.auth.StoredIDPUserCredentials;
import one.xis.context.DefaultComponent;

import java.util.Optional;

@DefaultComponent
public class SqlIDPCredentialRepository implements IDPCredentialRepository {

    private final IDPCredentialsSqlRepository repository;

    SqlIDPCredentialRepository(IDPCredentialsSqlRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<StoredIDPUserCredentials> findUserById(String userId) {
        return repository.findUserById(userId)
                .map(record -> new StoredIDPUserCredentials(record.getUserId(), record.getPasswordHash()));
    }

    @Override
    public void saveUser(StoredIDPUserCredentials credentials) {
        repository.saveUser(new IDPUserCredentialsRecord(credentials.getUserId(), credentials.getPasswordHash()));
    }

    @Override
    public Optional<StoredIDPClientCredentials> findClientById(String clientId) {
        return repository.findClientById(clientId)
                .map(record -> new StoredIDPClientCredentials(record.getClientId(), record.getClientSecretHash()));
    }

    @Override
    public void saveClient(StoredIDPClientCredentials credentials) {
        repository.saveClient(new IDPClientCredentialsRecord(credentials.getClientId(), credentials.getClientSecretHash()));
    }
}
