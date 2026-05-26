package one.xis.server;

import lombok.Data;
import one.xis.validation.ValidatorMessages;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Data
class ControllerMethodResult {

    private String nextURL;
    private String nextPageId;
    private String nextFrontletId;
    private String nextModalId;
    private boolean closeModal;
    private boolean reloadModalParent;
    private String frontletContainerId;
    private ActionProcessing actionProcessing;
    private final Collection<String> frontletsToReload = new HashSet<>();
    private Collection<String> updateEventKeys = new HashSet<>();
    private final Map<String, Object> frontletParameters = new HashMap<>();
    private final Map<String, Object> modalParameters = new HashMap<>();
    private final Map<String, Object> urlParameters = new HashMap<>();
    private final Map<String, Object> pathVariables = new HashMap<>();
    private final Map<String, Object> modelData = new HashMap<>();
    private final Map<String, Object> formData = new HashMap<>();
    private final Collection<String> returnedFormDataKeys = new HashSet<>();
    private boolean validationFailed;
    private final ValidatorMessages validatorMessages = new ValidatorMessages();
    private final Map<String, Object> requestScope = new HashMap<>();
    private final Map<String, Object> sessionStorage = new HashMap<>();
    private final Map<String, Object> localStorage = new HashMap<>();
    private final Map<String, Object> clientState = new HashMap<>();
    private String redirectUrl;
    private final Map<String, String> idVariables = new HashMap<>();
    private String annotatedTitle;
    private String annotatedAddress;


}
