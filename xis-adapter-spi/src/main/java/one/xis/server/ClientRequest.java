package one.xis.server;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import one.xis.ModelDataLoad;
import one.xis.gson.JsonMap;

import java.util.Locale;

@Data
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class ClientRequest {
    private String clientId;
    private String action;
    private String formBinding;
    private String pageId;
    private String pageUrl;
    private String frontletId;
    private String frontletContainerId;
    private Locale locale;
    private String zoneId;
    private RequestType type;
    private String accessToken;
    private String renewToken;
    private ModelDataLoad load = ModelDataLoad.INITIAL;

    private JsonMap formData = new JsonMap();
    private JsonMap pathVariables = new JsonMap();
    private JsonMap urlParameters = new JsonMap();
    private JsonMap queryParameters = new JsonMap();
    private JsonMap frontletParameters = new JsonMap();
    private JsonMap modalParameters = new JsonMap();
    private JsonMap actionParameters = new JsonMap();
    private final JsonMap sessionStorageData = new JsonMap();
    private final JsonMap localStorageData = new JsonMap();
    private final JsonMap clientStateData = new JsonMap();
    private final JsonMap globalVariableData = new JsonMap();
    private final JsonMap localDatabaseData = new JsonMap();

}
