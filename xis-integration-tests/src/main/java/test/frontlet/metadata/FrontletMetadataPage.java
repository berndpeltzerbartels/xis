package test.frontlet.metadata;

import one.xis.ModelData;
import one.xis.Page;

@Page("/frontlet-metadata-test.html")
class FrontletMetadataPage {

    @ModelData("title")
    String getTitle() {
        return "Frontlet Metadata Test Page";
    }
}
