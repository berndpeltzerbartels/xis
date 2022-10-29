package one.xis;

import lombok.Getter;
import one.xis.common.RequestIssue;

@Getter
public class ConnectorResponse {
    private final Object model;
    private final Class<?> nextComponent;
    private final RequestIssue requestIssue;

    ConnectorResponse(Object model, RequestIssue requestIssue) {
        this.model = model;
        this.nextComponent = null;
        this.requestIssue = requestIssue;
    }
}
