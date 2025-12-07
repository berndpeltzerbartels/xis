package test.widget.metadata;

import one.xis.ModelData;
import one.xis.Widget;

// This widget's annotation specifies containerB, but the HTML has default-widget pointing to containerA
// The annotation should win, ensuring the widget loads into containerB
@Widget(id = "DeepLinkWidget", url = "/deep-link", title = "Deep Link Title", containerId = "containerB")
class DeepLinkWidget {

    @ModelData("message")
    String getMessage() {
        return "Deep link widget in container B";
    }
}
