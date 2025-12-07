package test.widget.pathvariables;

import one.xis.ModelData;
import one.xis.PathVariable;
import one.xis.Widget;

@Widget(url = "/products/{category}.html", containerId = "container1")
class ProductWidget1 {

    @ModelData("category")
    String getCategory(@PathVariable("category") String category) {
        return category;
    }

    @ModelData("widget1Data")
    String getWidget1Data(@PathVariable("category") String category) {
        return "Widget1: " + category;
    }
}
