package test.widget;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import one.xis.Model;
import one.xis.Page;

@Page("/widgetPage.html")
@Setter
@RequiredArgsConstructor
class WidgetPage {
    private String widgetId;

    @Model("widgetId")
    String widgetId() {
        return widgetId;
    }
}
