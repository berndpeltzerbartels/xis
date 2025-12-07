package test.widget.metadata;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Widget;

@Widget(id = "WidgetMetadata", url = "/custom-url", title = "Custom Widget Title", containerId = "widgetContainer")
class WidgetMetadataWidget {

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
