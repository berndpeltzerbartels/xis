package test.widget.metadata;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Title;
import one.xis.Frontlet;

@Frontlet
class WidgetMetadataFrontlet {

    @Title
    String getTitle() {
        return "Custom Frontlet Title";
    }

    @ModelData("message")
    String getMessage() {
        return "Frontlet with metadata";
    }

    @Action
    Class<?> loadSecondWidget() {
        return SecondFrontlet.class;
    }

    @Action
    Class<?> loadDeepLinkWidget() {
        return DeepLinkFrontlet.class;
    }
}
