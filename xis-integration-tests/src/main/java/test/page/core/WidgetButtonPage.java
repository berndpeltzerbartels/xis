package test.page.core;

import lombok.Getter;
import one.xis.ModelData;
import one.xis.Page;

@Getter
@Page("/widgetButtonPage.html")
class WidgetButtonPage {

    private int invocations;

    @ModelData("title")
    String title() {
        invocations++;
        return "Widget Button Test";
    }
}
