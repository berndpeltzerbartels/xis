package one.xis.server;

import lombok.Data;
import one.xis.validation.ValidatorMessages;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Data
class ControllerResult {

    private String currentPageURL;
    private String currentWidgetId;
    private String nextURL;
    private String nextPageId;
    private Class<?> nextPageControllerClass;
    private String nextWidgetId;
    private String widgetContainerId;
    private Collection<String> widgetsToReload = new HashSet<>();
    private final Map<String, Object> bindingParameters = new HashMap<>();
    private final Map<String, Object> urlParameters = new HashMap<>();
    private final Map<String, Object> pathVariables = new HashMap<>();
    private final Map<String, Object> modelData = new HashMap<>();
    private final Map<String, Object> formData = new HashMap<>();
    private boolean validationFailed;
    private final ValidatorMessages validatorMessages = new ValidatorMessages();
    private final Map<String, Object> requestScope = new HashMap<>();
    private final Map<String, Object> clientState = new HashMap<>();
    private final Map<String, Object> localStorage = new HashMap<>();
    private String redirectUrl;

}
