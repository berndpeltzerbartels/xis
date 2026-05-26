package one.xis.server;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.JsonAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.validation.ValidatorMessages;

import java.util.*;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@JsonAdapter(ServerResponseTypeAdapter.class)

public class ServerResponse {

    private transient int status;
    private Map<String, Object> data = new HashMap<>();
    private Map<String, String> idVariables = new HashMap<>();
    private Map<String, Object> formData = new HashMap<>();
    private Collection<String> returnedFormDataKeys = new HashSet<>();
    private Map<String, Object> localStorageData = new HashMap<>();
    private Map<String, Object> localDatabaseData = new HashMap<>();
    private Map<String, Object> sessionStorageData = new HashMap<>();
    private Map<String, Object> clientStorageData = new HashMap<>();
    private Map<String, Object> frontletParameters = new HashMap<>();
    private Map<String, Object> modalParameters = new HashMap<>();
    private boolean authenticated;
    private Collection<String> userRoles = new HashSet<>();
    private Collection<String> updateEventKeys = new ArrayList<>();
    private Collection<String> reloadFrontlets = new ArrayList<>(); // TODO remove?
    private String nextURL;
    private String nextFrontletId;
    private String nextModalId;
    private boolean closeModal;
    private boolean reloadModalParent;
    private ValidatorMessages validatorMessages = new ValidatorMessages();
    private boolean reloadPage; // TODO do we need this?
    private String frontletContainerId;
    private String redirectUrl;
    private ActionProcessing actionProcessing = ActionProcessing.NONE;
    private String annotatedTitle;
    private String annotatedAddress;
    private Collection<DefaultFrontlet> defaultFrontlets = new HashSet<>();

    void clear() {
        // do not clear store data
        status = 0;
        data.clear();
        formData.clear();
        returnedFormDataKeys.clear();
        reloadFrontlets.clear();
        frontletParameters.clear();
        modalParameters.clear();
        nextURL = null;
        nextFrontletId = null;
        nextModalId = null;
        closeModal = false;
        reloadModalParent = false;
        validatorMessages = new ValidatorMessages();
        reloadPage = false;
        frontletContainerId = null;
        defaultFrontlets.clear();
    }
}
