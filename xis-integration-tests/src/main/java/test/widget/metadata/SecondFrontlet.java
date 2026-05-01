package test.widget.metadata;

import one.xis.ModelData;
import one.xis.Frontlet;

@Frontlet(id = "SecondWidget", url = "/second-url", title = "Second Frontlet Title")
class SecondFrontlet {

    @ModelData("message")
    String getMessage() {
        return "Second widget loaded";
    }
}
