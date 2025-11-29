package test.widget;

import one.xis.ModelData;
import one.xis.Page;

@Page("/widgetContainerParameterPage.html")
class WidgetContainerParameterPage {

    @ModelData("categoryValue")
    String categoryValue() {
        return "electronics";
    }

    @ModelData("sortValue")
    String sortValue() {
        return "price";
    }
}
