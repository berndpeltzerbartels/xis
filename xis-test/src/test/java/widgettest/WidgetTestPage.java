package widgettest;

import one.xis.Page;
import one.xis.QueryParameter;

/**
 * Generic test page for loading widgets dynamically.
 * Usage: openPage("/widget-test.html?widgetClass=com.example.MyWidget")
 */
@Page("/widget-test.html")
public class WidgetTestPage {
    
    private final Class<?> widgetClass;
    
    public WidgetTestPage(@QueryParameter(value = "widgetClass", required = false) String widgetClassName) {
        if (widgetClassName != null) {
            try {
                this.widgetClass = Class.forName(widgetClassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalArgumentException("Widget class not found: " + widgetClassName, e);
            }
        } else {
            this.widgetClass = null;
        }
    }
    
    public Class<?> getDefaultWidget() {
        return widgetClass;
    }
}
