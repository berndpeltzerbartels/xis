package one.xis.server;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Data
public class ClientRequest {
    private String clientId;
    private String userId;
    private String action;
    private String formBinding;
    private String pageId;
    private String widgetId;
    private String widgetContainerId;
    private Locale locale;
    private String zoneId;
    private RequestType type;

    @JsonDeserialize(using = MapDeserializer.class)
    private Map<String, String> formData = new HashMap<>();

    @JsonDeserialize(using = MapDeserializer.class)
    private Map<String, String> pathVariables = new HashMap<>();

    @JsonDeserialize(using = MapDeserializer.class)
    private Map<String, String> urlParameters = new HashMap<>();

    @JsonDeserialize(using = MapDeserializer.class)
    private Map<String, String> bindingParameters = new HashMap<>();

    @JsonDeserialize(using = MapDeserializer.class)
    private Map<String, String> actionParameters = new HashMap<>();

    @JsonDeserialize(using = MapDeserializer.class)
    private final Map<String, String> clientStateData = new HashMap<>();

    @JsonDeserialize(using = MapDeserializer.class)
    private final Map<String, String> localStorageData = new HashMap<>();

    @JsonDeserialize(using = MapDeserializer.class)
    private final Map<String, String> localDatabaseData = new HashMap<>();


}
