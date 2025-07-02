package one.xis.ipdclient;

import lombok.Data;

import java.util.Collection;

@Data
public class IDPPublicKeyResponse {

    private Collection<JsonWebKey> keys;

}


