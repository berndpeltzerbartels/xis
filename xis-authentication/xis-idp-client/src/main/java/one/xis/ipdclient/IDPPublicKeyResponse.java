package one.xis.ipdclient;

import lombok.Data;
import one.xis.auth.JsonWebKey;

import java.util.Collection;

@Data
public class IDPPublicKeyResponse {

    private Collection<JsonWebKey> keys;

}


