package spring.test;

import one.xis.idp.*;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


@Component
class IDPTestService implements IDPService {

    private static final IDPClientInfo TEST_CLIENT_INFO = new IDPClientInfoImpl(
            "idp-client-id",
            "idp-client-secret",
            Set.of("http://localhost:8080/xis/auth/callback", "http://localhost:8080/another/callback"));

    @Override
    public Optional<IDPUserInfo> findUserInfo(String userId) {
        if (userId.equals("admin")) {
            return Optional.of(new IDPUserInfoImpl("admin", "idp-client-id", "admin123", "admin@admin.de", Set.of("admin", "user"), Map.of()));
        }
        return Optional.empty();
    }

    @Override
    public Optional<IDPClientInfo> findClientInfo(String clientId) {
        if (clientId.equals("idp-client-id")) {
            return Optional.of(TEST_CLIENT_INFO);
        }
        return Optional.empty();
    }

    @Override
    public IDPClientInfo createClientInfo(Collection<String> permittedRedirectUrls) {
        return TEST_CLIENT_INFO;
    }
}
