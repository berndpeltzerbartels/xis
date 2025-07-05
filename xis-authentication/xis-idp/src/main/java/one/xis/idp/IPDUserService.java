package one.xis.idp;

import java.util.Optional;

public interface IPDUserService {

    Optional<IDPUserInfo> findUserInfo(String userId);
    
}
