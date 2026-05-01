package one.xis.server;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.validation.ValidatorMessages;

import java.util.*;


//TODO do not serialize null fields. requires edits in client, too
@Data
@AllArgsConstructor
@NoArgsConstructor

public class ServerResponse {

    private transient int status;
    private Map<String, Object> data = new HashMap<>();
    private Map<String, String> idVariables = new HashMap<>();
    private Map<String, Object> formData = new HashMap<>();
    private Map<String, Object> localStorageData = new HashMap<>();
    private Map<String, Object> localDatabaseData = new HashMap<>();
    private Map<String, Object> sessionStorageData = new HashMap<>();
    private Map<String, Object> clientStorageData = new HashMap<>();
    private Map<String, Object> frontletParameters = new HashMap<>();
    private Collection<String> updateEventKeys = new ArrayList<>();
    private Collection<String> reloadFrontlets = new ArrayList<>(); // TODO remove?
    private String nextURL;
    private String nextFrontletId;
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
        reloadFrontlets.clear();
        frontletParameters.clear();
        nextURL = null;
        nextFrontletId = null;
        validatorMessages = new ValidatorMessages();
        reloadPage = false;
        frontletContainerId = null;
        defaultFrontlets.clear();
    }
}
