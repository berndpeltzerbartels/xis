package test.widget.metadata;

import one.xis.ModelData;
import one.xis.Widget;

@Widget(id = "SecondWidget", url = "/second-url", title = "Second Widget Title")
class SecondWidget {

    @ModelData("message")
    String getMessage() {
        return "Second widget loaded";
    }
}
