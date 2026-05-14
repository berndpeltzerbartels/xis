package test.frontlet.metadata;

import one.xis.ModelData;
import one.xis.Frontlet;

// This frontlet's annotation specifies containerB, but the HTML has default-frontlet pointing to containerA
// The annotation should win, ensuring the frontlet loads into containerB
@Frontlet(id = "DeepLinkFrontlet", url = "/deep-link", title = "Deep Link Title", containerId = "containerB")
class DeepLinkFrontlet {

    @ModelData("message")
    String getMessage() {
        return "Deep link frontlet in container B";
    }
}
