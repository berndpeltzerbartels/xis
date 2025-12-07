package test.widget.metadata;

import one.xis.ModelData;
import one.xis.Page;

@Page("/widget-metadata-test.html")
class WidgetMetadataPage {

    @ModelData("title")
    String getTitle() {
        return "Widget Metadata Test Page";
    }
}
