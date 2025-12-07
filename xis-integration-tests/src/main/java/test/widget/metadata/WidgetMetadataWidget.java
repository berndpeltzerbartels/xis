package test.widget.metadata;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Title;
import one.xis.Widget;

@Widget
class WidgetMetadataWidget {

    @Title
    String getTitle() {
        return "Custom Widget Title";
    }

    @ModelData("message")
    String getMessage() {
        return "Widget with metadata";
    }

    @Action
    Class<?> loadSecondWidget() {
        return SecondWidget.class;
    }

    @Action
    Class<?> loadDeepLinkWidget() {
        return DeepLinkWidget.class;
    }
}
