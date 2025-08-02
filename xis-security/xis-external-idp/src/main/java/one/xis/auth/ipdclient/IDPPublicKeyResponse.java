package one.xis.auth.ipdclient;

import lombok.Data;
import one.xis.auth.JsonWebKey;

import java.util.Collection;

@Data
public class IDPPublicKeyResponse {

    private final Collection<JsonWebKey> keys;

}


