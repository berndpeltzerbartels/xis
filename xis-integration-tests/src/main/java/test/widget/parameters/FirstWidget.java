package test.widget.parameters;

import one.xis.Action;
import one.xis.Widget;
import one.xis.WidgetResponse;

@Widget
public class FirstWidget {

    @Action("switchWidget")
    public WidgetResponse switchWidget() {
        return new WidgetResponse(SecondWidget.class)
                .widgetParameter("actionParam", "actionValue");
    }
}
