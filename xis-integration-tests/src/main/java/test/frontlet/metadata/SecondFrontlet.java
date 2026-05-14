package test.frontlet.metadata;

import one.xis.ModelData;
import one.xis.Frontlet;

@Frontlet(id = "SecondFrontlet", url = "/second-url", title = "Second Frontlet Title")
class SecondFrontlet {

    @ModelData("message")
    String getMessage() {
        return "Second frontlet loaded";
    }
}
