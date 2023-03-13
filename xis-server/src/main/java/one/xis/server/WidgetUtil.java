package one.xis.server;

import lombok.NonNull;
import one.xis.Widget;

class WidgetUtil {
    static String getId(Object widgetController) {
        @NonNull var anno = widgetController.getClass().getAnnotation(Widget.class);
        return anno.value().isEmpty() ? widgetController.getClass().getSimpleName() : anno.value();
    }
}
