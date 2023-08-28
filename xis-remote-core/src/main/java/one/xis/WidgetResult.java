package one.xis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class WidgetResult {
    private final Class<?> controllerClass;
    private String targetContainer;
    private final Map<String, Object> widgetParameters = new HashMap<>();

    public WidgetResult withWidgetParameter(String name, Object value) {
        widgetParameters.put(name, value);
        return this;
    }

    public static WidgetResult of(Class<?> controllerClass, String paramName, Object paramValue) {
        return new WidgetResult(controllerClass).withWidgetParameter(paramName, paramValue);
    }
}
