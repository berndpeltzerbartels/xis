package one.ajax;

import lombok.Data;
import one.xis.dto.ComponentType;
import one.xis.dto.RequestContext;
import one.xis.dto.RequestIssue;

import java.util.Map;

@Data
public class ConnectorRequest implements RequestContext {
    private RequestIssue issue;
    private String action;
    private Object state;
    private String clientId;
    private String token;
    private String javaClassId;
    private ComponentType componentType;
    private Map<String, Object> states;
    private Object model;

    @Override
    public Object getState(String name) {
        return states.get(name);
    }
}
