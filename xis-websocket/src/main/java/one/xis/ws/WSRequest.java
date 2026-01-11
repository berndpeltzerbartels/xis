package one.xis.ws;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
abstract class WSRequest {
    private WSRequestHeaders headers = new WSRequestHeaders();
    private Map<String, String> parameters = new HashMap<>();
    private Long messageId;
    private String path;
    private String method;
    private Map<String, String> queryParameters = new HashMap<>();
}
