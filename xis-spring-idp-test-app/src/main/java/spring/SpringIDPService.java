package spring;

import one.xis.auth.idp.IDPClientInfo;
import one.xis.auth.idp.IDPService;
import one.xis.auth.idp.IDPUserInfo;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpringIDPService implements IDPService {
    @Override
    public Optional<IDPUserInfo> findUserInfo(String userId) {
        return Optional.empty();
    }

    @Override
    public Optional<IDPClientInfo> findClientInfo(String clientId) {
        return Optional.empty();
    }

    @Override
    public boolean validateCredentials(String username, String password) {
        return false;
    }

    @Override
    public boolean validateClientSecret(String clientId, String clientSecret) {
        return false;
    }
}
