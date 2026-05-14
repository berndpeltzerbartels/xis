package one.xis.server;

import lombok.Data;

import java.util.Map;

@Data
class PageControllerMatch {
    private final ControllerWrapper pageControllerWrapper;
    private final Map<String, String> pathVariables;
    private final Map<String, String> queryParameters;

}
