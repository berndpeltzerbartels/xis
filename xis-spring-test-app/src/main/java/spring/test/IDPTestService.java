package spring.test;

import one.xis.idp.*;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


@Component
class IDPTestService implements IDPService {

    private static final String CLIENT_ID = "idp-client-id";
    private static final String CLIENT_SECRET = "idp-client-secret";

    private IDPClientInfo clientInfo;

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
            return Optional.of(clientInfo);
        }
        return Optional.empty();
    }

    @Override
    public IDPClientInfo createClientInfo(Collection<String> permittedRedirectUrls) {
        clientInfo = new IDPClientInfoImpl(CLIENT_ID, CLIENT_SECRET, permittedRedirectUrls);
        return clientInfo;
    }
}
