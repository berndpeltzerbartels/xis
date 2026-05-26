package one.xis.server;

import lombok.Data;
import one.xis.ToastMessage;
import one.xis.validation.ValidatorMessages;

import java.util.*;

@Data
class ControllerResult {

    private String currentPageURL;
    private String currentFrontletId;
    private String nextURL;
    private String nextPageId;
    private Class<?> nextPageControllerClass;
    private String nextFrontletId;
    private String nextModalId;
    private boolean closeModal;
    private boolean reloadModalParent;
    private String frontletContainerId;
    private String annotatedTitle;
    private String annotatedAddress;
    private ActionProcessing actionProcessing = ActionProcessing.NONE;
    private Collection<String> frontletsToReload = new HashSet<>();
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
    private final Collection<ToastMessage> toastMessages = new ArrayList<>();
    private final Map<String, Object> globalVariables = new HashMap<>();
    private String redirectUrl;
    private final Map<String, String> tagVariables = new HashMap<>();
    private final Map<String, String> idVariables = new HashMap<>();

}
