package one.xis;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class PageResponse {
    private final Class<?> controllerClass;
    private final Map<String, String> pathVariables = new HashMap<>();
    private final Map<String, String> urlParameters = new HashMap<>();

    public PageResponse pathVariable(@NonNull String name, @NonNull Object value) {
        pathVariables.put(name, asString(value));
        return this;
    }

    public PageResponse urlParameter(@NonNull String name, @NonNull Object value) {
        pathVariables.put(name, asString(value));
        return this;
    }

    public static PageResponse of(@NonNull Class<?> controllerClass, @NonNull String pathVariable, @NonNull Object pathVariabelValue) {
        return new PageResponse(controllerClass).pathVariable(pathVariable, asString(pathVariabelValue));
    }

    private static String asString(@NonNull Object o) {
        if (o instanceof String str) {
            return str;
        }
        return o.toString();
    }
}
