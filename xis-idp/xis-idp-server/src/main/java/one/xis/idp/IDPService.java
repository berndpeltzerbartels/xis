package one.xis.idp;

import one.xis.ImportInstances;

import java.util.Optional;
import java.util.Set;

@ImportInstances
public interface IDPService {

    Optional<IDPUserInfo> findUserInfo(String userId);

    Optional<IDPClientInfo> findClientInfo(String clientId);

    IDPClientInfo createClientInfo(String clientId, String clientSecret, Set<String> permittedRedirectUrls);

}
