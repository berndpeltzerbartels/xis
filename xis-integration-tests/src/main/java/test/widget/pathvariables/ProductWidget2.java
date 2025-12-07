package test.widget.pathvariables;

import one.xis.ModelData;
import one.xis.PathVariable;
import one.xis.Widget;

@Widget(url = "/products/{category}.html", containerId = "container2")
class ProductWidget2 {

    @ModelData("category")
    String getCategory(@PathVariable("category") String category) {
        return category;
    }

    @ModelData("widget2Data")
    String widget2Data(@PathVariable("category") String category) {
        return "Widget2: " + category;
    }
}
