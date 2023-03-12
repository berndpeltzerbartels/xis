package one.xis.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class Request {
    private Map<String, DataItem> data;
    private String clientId;
    private String userId;
    private String key; // action-key or model-key
    private String controllerId;

    @JsonProperty("type")
    private ControllerType controllerType;
}
