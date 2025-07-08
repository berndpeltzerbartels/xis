package one.xis.idp;

import one.xis.ImportInstances;

import java.util.Optional;

@ImportInstances
public interface IDPUserService {

    Optional<IDPUserInfo> findUserInfo(String userId);

}
