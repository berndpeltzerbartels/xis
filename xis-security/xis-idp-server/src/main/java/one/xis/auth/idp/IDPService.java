package one.xis.auth.idp;

import one.xis.ImportInstances;

import java.util.Optional;

@ImportInstances
public interface IDPService {

    Optional<IDPUserInfo> findUserInfo(String userId);

    Optional<IDPClientInfo> findClientInfo(String clientId);

    boolean validateCredentials(String username, String password);

    boolean validateClientSecret(String clientId, String clientSecret);


}
