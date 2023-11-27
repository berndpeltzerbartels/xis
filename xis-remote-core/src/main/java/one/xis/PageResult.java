package one.xis;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class PageResult {
    private final Class<?> controllerClass;
    private final Map<String, String> pathVariables = new HashMap<>();
    private final Map<String, String> urlParameters = new HashMap<>();

    public PageResult withPathVariable(@NonNull String name, @NonNull Object value) {
        pathVariables.put(name, asString(value));
        return this;
    }

    public PageResult withUrlParameter(@NonNull String name, @NonNull Object value) {
        pathVariables.put(name, asString(value));
        return this;
    }

    public static PageResult of(@NonNull Class<?> controllerClass, @NonNull String pathVariable, @NonNull Object pathVariabelValue) {
        return new PageResult(controllerClass).withPathVariable(pathVariable, asString(pathVariabelValue));
    }

    private static String asString(@NonNull Object o) {
        if (o instanceof String str) {
            return str;
        }
        return o.toString();
    }
}
