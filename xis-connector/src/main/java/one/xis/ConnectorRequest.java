package one.xis;

import lombok.Data;
import one.xis.common.ComponentType;
import one.xis.common.RequestContext;
import one.xis.common.RequestIssue;

import java.util.List;

@Data
public class ConnectorRequest implements RequestContext {
    private RequestIssue issue;
    private List<String> signatures;
    private Object state;
    private String clientId;
    private String token;
    private String javaClassId;
    private ComponentType componentType;
}
