package one.xis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class PageResult {
    private final Class<?> controllerClass;
    private final Map<String, Object> pathVariables = new HashMap<>();
    private final Map<String, Object> urlParameters = new HashMap<>();

    public PageResult withPathVariable(String name, Object value) {
        pathVariables.put(name, value);
        return this;
    }

    public PageResult withUrlParameter(String name, Object value) {
        pathVariables.put(name, value);
        return this;
    }

    public static PageResult of(Class<?> controllerClass, String pathVariable, Object pathVariabelValue) {
        return new PageResult(controllerClass).withPathVariable(pathVariable, pathVariabelValue);
    }
}
