package one.xis;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a response that navigates to a specific page (controller) with optional path variables and query parameters.
 * Can be used within pages or widgets.
 */
@Getter
@RequiredArgsConstructor
public class PageResponse implements Response {
    private final Class<?> controllerClass;
    private final Map<String, Object> pathVariables = new HashMap<>();
    private final Map<String, Object> queryParameters = new HashMap<>();

    public PageResponse pathVariable(@NonNull String name, @NonNull Object value) {
        pathVariables.put(name, asString(value));
        return this;
    }

    public PageResponse queryParameter(@NonNull String name, @NonNull Object value) {
        queryParameters.put(name, asString(value));
        return this;
    }

    public static PageResponse of(@NonNull Class<?> controllerClass, @NonNull String pathVariable, @NonNull Object pathVariableValue) {
        return new PageResponse(controllerClass).pathVariable(pathVariable, asString(pathVariableValue));
    }

    private static String asString(@NonNull Object o) {
        if (o instanceof String str) {
            return str;
        }
        return o.toString();
    }
}
