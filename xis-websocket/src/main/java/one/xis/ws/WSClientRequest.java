package one.xis.ws;

import lombok.Data;
import lombok.EqualsAndHashCode;
import one.xis.server.ClientRequest;

@Data
@EqualsAndHashCode(callSuper = true)
public class WSClientRequest extends WSRequest {
    private ClientRequest body = new ClientRequest();
}
