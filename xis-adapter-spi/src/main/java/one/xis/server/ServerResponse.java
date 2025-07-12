package one.xis.server;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.auth.token.ApiTokens;
import one.xis.validation.ValidatorMessages;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


//TODO do not serialize null fields. requires edits in client, too
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServerResponse {
    
    private transient int status;
    private Map<String, Object> data = new HashMap<>();
    private Map<String, Object> formData = new HashMap<>();
    private Map<String, Object> localStorageData = new HashMap<>();
    private Map<String, Object> localDatabaseData = new HashMap<>();
    private Map<String, Object> clientStateData = new HashMap<>();
    private Collection<String> reloadWidgets = new ArrayList<>();
    private String nextURL;
    private String nextWidgetId;
    private ValidatorMessages validatorMessages = new ValidatorMessages();
    private boolean reloadPage; // TODO do we need this?
    private String widgetContainerId;
    private transient ApiTokens tokens;
    private String redirectUrl;

    void clear() {
        // do not clear store data
        status = 0;
        data.clear();
        formData.clear();
        reloadWidgets.clear();
        nextURL = null;
        nextWidgetId = null;
        validatorMessages = new ValidatorMessages();
        reloadPage = false;
        widgetContainerId = null;
        tokens = null;
    }
}
