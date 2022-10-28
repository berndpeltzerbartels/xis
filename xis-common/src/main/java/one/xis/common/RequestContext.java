package one.xis.common;

public interface RequestContext {
    RequestIssue getIssue();

    java.util.List<String> getSignatures();

    Object getState();

    String getClientId();

    String getToken();

    String getJavaClassId();

    ComponentType getComponentType();

    void setIssue(RequestIssue issue);

    void setSignatures(java.util.List<String> signatures);

    void setState(Object state);

    void setClientId(String clientId);

    void setToken(String token);

    void setJavaClassId(String javaClassId);

    void setComponentType(ComponentType componentType);
}
