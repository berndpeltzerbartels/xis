package test.frontlet.metadata;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Title;
import one.xis.Frontlet;

@Frontlet
class FrontletMetadataFrontlet {

    @Title
    String getTitle() {
        return "Custom Frontlet Title";
    }

    @ModelData("message")
    String getMessage() {
        return "Frontlet with metadata";
    }

    @Action
    Class<?> loadSecondFrontlet() {
        return SecondFrontlet.class;
    }

    @Action
    Class<?> loadDeepLinkFrontlet() {
        return DeepLinkFrontlet.class;
    }
}
