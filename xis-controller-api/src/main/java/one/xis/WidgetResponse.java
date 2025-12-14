package one.xis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Getter
@EqualsAndHashCode
public class WidgetResponse implements Response {
    private Class<?> controllerClass;
    private String targetContainer; // TODO
    private final Map<String, Object> pathVariables = new HashMap<>();
    private final Map<String, Object> widgetParameters = new HashMap<>();
    private final Collection<String> widgetsToReload = new HashSet<>(); // TODO

    public WidgetResponse(@NonNull Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }

    public WidgetResponse(@NonNull Class<?> controllerClass, @NonNull String targetContainer) {
        this.controllerClass = controllerClass;
        this.targetContainer = targetContainer;
    }


    public WidgetResponse() {

    }


    public WidgetResponse controllerClass(@NonNull Class<?> controllerClass) {
        this.controllerClass = controllerClass;
        return this;
    }

    public WidgetResponse widgetParameter(@NonNull String name, @NonNull Object value) {
        widgetParameters.put(name, asString(value));
        return this;
    }

    public WidgetResponse targetContainer(String targetContainer) {
        this.targetContainer = targetContainer;
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
