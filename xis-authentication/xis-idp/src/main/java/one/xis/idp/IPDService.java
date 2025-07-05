package one.xis.idp;

import java.util.Optional;

public interface IPDService {

    Optional<String> clientSecret(String clientId);

}
