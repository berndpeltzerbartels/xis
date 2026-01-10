package one.xis.ws;

import lombok.Data;
import lombok.EqualsAndHashCode;
import one.xis.server.ClientRequest;

@Data
@EqualsAndHashCode(callSuper = true)
class WSClientRequest extends WSRequest {
    private final ClientRequest clientRequest = new ClientRequest();
}
