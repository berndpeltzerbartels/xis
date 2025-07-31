package one.xis.server;

import lombok.Data;
import one.xis.gson.JsonMap;

import java.util.Locale;

@Data
public class ClientRequest {
    private String clientId;
    private String action;
    private String formBinding;
    private String pageId;
    private String pageUrl;
    private String widgetId;
    private String widgetContainerId;
    private Locale locale;
    private String zoneId;
    private RequestType type;
    private String accessToken;
    private String renewToken;

    private JsonMap formData = new JsonMap();
    private JsonMap pathVariables = new JsonMap();
    private JsonMap urlParameters = new JsonMap();
    private JsonMap queryParameters = new JsonMap();
    private JsonMap bindingParameters = new JsonMap();
    private JsonMap actionParameters = new JsonMap();
    private final JsonMap clientStateData = new JsonMap();
    private final JsonMap localStorageData = new JsonMap();
    private final JsonMap localDatabaseData = new JsonMap();

}
