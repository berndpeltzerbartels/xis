package one.xis.ws;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
abstract class WSRequest {
    private String uri;
    private final WSRequestHeaders headers = new WSRequestHeaders();
    private final Map<String, String> parameters = new HashMap<>();
}
