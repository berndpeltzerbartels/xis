package one.xis.idp;

import one.xis.ImportInstances;

import java.util.Optional;

@ImportInstances
public interface IDPService {

    Optional<IDPUserInfo> findUserInfo(String userId);

    Optional<IDPClientInfo> findClientInfo(String clientId);


}
