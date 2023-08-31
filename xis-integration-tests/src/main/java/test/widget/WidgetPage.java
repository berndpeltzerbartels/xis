package test.widget;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.ModelData;
import one.xis.Page;

@Page("/widgetPage.html")
@Setter
@RequiredArgsConstructor
class WidgetPage {
    private String widgetId;

    @ModelData("widgetId")
    String widgetId() {
        return widgetId;
    }
}
