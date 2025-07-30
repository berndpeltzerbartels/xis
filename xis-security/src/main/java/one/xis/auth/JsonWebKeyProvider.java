package one.xis.auth;

import java.util.Collection;
import java.util.Map;

public interface JsonWebKeyProvider {

    Map<String, Collection<JsonWebKey>> getKeysForIssuer(String issuer);
}
