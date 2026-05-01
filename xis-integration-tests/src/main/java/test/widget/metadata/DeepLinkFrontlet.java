package test.widget.metadata;

import one.xis.ModelData;
import one.xis.Frontlet;

// This widget's annotation specifies containerB, but the HTML has default-widget pointing to containerA
// The annotation should win, ensuring the widget loads into containerB
@Frontlet(id = "DeepLinkWidget", url = "/deep-link", title = "Deep Link Title", containerId = "containerB")
class DeepLinkFrontlet {

    @ModelData("message")
    String getMessage() {
        return "Deep link widget in container B";
    }
}
