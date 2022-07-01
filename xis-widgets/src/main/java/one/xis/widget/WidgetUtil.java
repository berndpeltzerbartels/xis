package one.xis.widget;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import one.xis.Widget;

@UtilityClass
public class WidgetUtil {

    public String getWidgetId(@NonNull Object widgetController) {
        return getWidgetId(widgetController.getClass());
    }

    public String getWidgetId(@NonNull Class<?> widgetControllerClass) {
        String annoValue = widgetControllerClass.getAnnotation(Widget.class).value();
        return annoValue.isEmpty() ? widgetControllerClass.getName() : annoValue;
    }
}
