package test.page.core;

import lombok.Getter;
import one.xis.ModelData;
import one.xis.Frontlet;

@Getter
@Frontlet
class WidgetButton1 {

    private int invocations;

    @ModelData("content")
    String content() {
        invocations++;
        return "Frontlet 1 Content";
    }
}
