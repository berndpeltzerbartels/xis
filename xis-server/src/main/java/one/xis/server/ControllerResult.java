package one.xis.server;

import lombok.Data;
import one.xis.validation.ValidatorMessages;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Data
class ControllerResult {

    private String nextPageURL;
    private String nextWidgetId;
    private String widgetContainerId;
    private Collection<String> widgetsToReload;
    private Map<String, Object> widgetParameters = new HashMap<>();
    private Map<String, Object> urlParameters = new HashMap<>();
    private Map<String, Object> pathVariables = new HashMap<>();
    private Map<String, Object> modelData = new HashMap<>();
    private boolean validationFailed;
    private final ValidatorMessages validatorMessages = new ValidatorMessages();

}
