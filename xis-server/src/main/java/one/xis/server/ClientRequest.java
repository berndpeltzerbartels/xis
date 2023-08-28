package one.xis.server;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.Map;

@Data
public class ClientRequest {
    private String clientId;
    private String userId;
    private String action;
    private String pageId;
    private String widgetId;

    @JsonDeserialize(using = MapDeserializer.class)
    private Map<String, String> data;

    @JsonDeserialize(using = MapDeserializer.class)
    private Map<String, String> parameters;

    @JsonDeserialize(using = MapDeserializer.class)
    private Map<String, Object> pathVariables;

    @JsonDeserialize(using = MapDeserializer.class)
    private Map<String, Object> urlParameters;

    @JsonDeserialize(using = MapDeserializer.class)
    private Map<String, Object> widgetParameters;
}
