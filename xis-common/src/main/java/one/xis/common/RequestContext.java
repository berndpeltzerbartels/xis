package one.xis.common;

import java.util.Map;

public interface RequestContext {
    RequestIssue getIssue();

    void setModel(Object model);

    Object getModel();

    String getAction();

    Object getState(String name);

    String getClientId();

    String getToken();

    String getJavaClassId();

    ComponentType getComponentType();

    void setIssue(RequestIssue issue);

    void setAction(String action);

    void setStates(Map<String, Object> states);

    void setClientId(String clientId);

    void setToken(String token);

    void setJavaClassId(String javaClassId);

    void setComponentType(ComponentType componentType);
}
