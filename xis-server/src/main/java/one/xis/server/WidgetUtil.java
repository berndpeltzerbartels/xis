package one.xis.server;

import lombok.NonNull;
import one.xis.Widget;
import one.xis.context.AppContext;

import java.util.Collection;
import java.util.stream.Collectors;

public class WidgetUtil {
    public static String getId(@NonNull Object widgetController) {
        return getId(widgetController.getClass());
    }

    public static String getId(@NonNull Class<?> controllerClass) {
        @NonNull var anno = controllerClass.getAnnotation(Widget.class);
        return anno.value().isEmpty() ? controllerClass.getSimpleName() : anno.value();
    }

    public static Collection<Object> getAllWidgetControllers() {
        return AppContext.getInstance("one.xis").getSingletons().stream()
                .filter(o -> o.getClass().isAnnotationPresent(Widget.class))
                .collect(Collectors.toSet());
    }
}
