package test.page.core;

import lombok.Getter;
import one.xis.ModelData;
import one.xis.Widget;

@Getter
@Widget
class WidgetButton2 {

    private int invocations;

    @ModelData("content")
    String content() {
        invocations++;
        return "Widget 2 Content";
    }
}
