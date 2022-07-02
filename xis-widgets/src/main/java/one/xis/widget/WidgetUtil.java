package one.xis.widget;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WidgetUtil {

    public String getWidgetId(@NonNull Object widgetController) {
        return getWidgetId(widgetController.getClass());
    }

    public String getWidgetId(@NonNull Class<?> widgetControllerClass) {
        return widgetControllerClass.getName();
    }
}
