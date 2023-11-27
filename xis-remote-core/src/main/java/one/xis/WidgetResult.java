package one.xis;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class WidgetResult {
    private final Class<?> controllerClass;
    private String targetContainer; // TODO
    private final Map<String, String> widgetParameters = new HashMap<>();

    public WidgetResult withWidgetParameter(@NonNull String name, @NonNull Object value) {
        widgetParameters.put(name, asString(value));
        return this;
    }

    public static WidgetResult of(@NonNull Class<?> controllerClass, @NonNull String paramName, @NonNull Object paramValue) {
        return new WidgetResult(controllerClass).withWidgetParameter(paramName, asString(paramValue));
    }

    private static String asString(@NonNull Object o) {
        if (o instanceof String str) {
            return str;
        }
        return o.toString();
    }
}
