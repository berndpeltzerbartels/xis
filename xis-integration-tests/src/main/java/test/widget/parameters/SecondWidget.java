package test.widget.parameters;

import one.xis.ModelData;
import one.xis.Widget;
import one.xis.WidgetParameter;

@Widget
public class SecondWidget {

    @ModelData
    public String combinedParams(
            @WidgetParameter("actionParam") String actionParam,
            @WidgetParameter("containerParam") String containerParam) {
        return "Action: " + actionParam + ", Container: " + containerParam;
    }

    @ModelData
    public String actionParam(@WidgetParameter("actionParam") String actionParam) {
        return actionParam;
    }

    @ModelData
    public String containerParam(@WidgetParameter("containerParam") String containerParam) {
        return containerParam;
    }
}
