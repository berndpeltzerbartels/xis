package spring.test;

import one.xis.idp.IDPClientInfo;
import one.xis.idp.IDPService;
import one.xis.idp.IDPUserInfo;
import one.xis.idp.IDPUserInfoImpl;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.Set;


@Component
class IDPTestService implements IDPService {

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


}
