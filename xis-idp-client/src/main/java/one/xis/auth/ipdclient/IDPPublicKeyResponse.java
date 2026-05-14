package one.xis.auth.ipdclient;

import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.auth.JsonWebKey;

import java.util.Collection;

@Data
@NoArgsConstructor
public class IDPPublicKeyResponse {

    private Collection<JsonWebKey> keys;

}

