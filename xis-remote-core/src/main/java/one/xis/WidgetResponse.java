package one.xis;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class WidgetResponse {
    private final Class<?> controllerClass;
    private String targetContainer; // TODO
    private final Map<String, String> widgetParameters = new HashMap<>();
    private final Collection<String> widgetsToReload = new HashSet<>(); // TODO

    public WidgetResponse widgetParameter(@NonNull String name, @NonNull Object value) {
        widgetParameters.put(name, asString(value));
        return this;
    }

    public WidgetResponse targeContainer(String targeteContainer) {
        this.targetContainer = targeteContainer;
        return this;
    }

    public WidgetResponse reloadWidget(Class<?> widgetController) {
        if (!widgetController.isAnnotationPresent(Widget.class)) {
            throw new IllegalArgumentException("not a widget: " + widgetController);
        }
        var widgetAnnotation = widgetController.getAnnotation(Widget.class);
        String widgetId = widgetAnnotation.value().equals("") ? widgetController.getSimpleName() : widgetAnnotation.value();
        return reloadWidget(widgetId);
    }

    public WidgetResponse reloadWidget(String widgetId) {
        widgetsToReload.add(widgetId);
        return this;
    }

    public static WidgetResponse of(@NonNull Class<?> controllerClass, @NonNull String paramName, @NonNull Object paramValue) {
        return new WidgetResponse(controllerClass).widgetParameter(paramName, asString(paramValue));
    }

    private static String asString(@NonNull Object o) {
        if (o instanceof String str) {
            return str;
        }
        return o.toString();
    }
}
