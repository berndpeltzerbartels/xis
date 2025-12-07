package test.widget.metadata;

import one.xis.ModelData;
import one.xis.Page;

@Page("/deep-linking-test.html")
class DeepLinkingPage {

    @ModelData("title")
    String getTitle() {
        return "Deep Linking Test Page";
    }
}
