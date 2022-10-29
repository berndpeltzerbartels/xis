package one.xis;

import lombok.Data;
import one.xis.common.ComponentType;
import one.xis.common.RequestContext;
import one.xis.common.RequestIssue;

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
