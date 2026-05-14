package test.frontlet.pathvariables;

import one.xis.ModelData;
import one.xis.PathVariable;
import one.xis.Frontlet;

@Frontlet(url = "/products/{category}.html", containerId = "container2")
class ProductFrontlet2 {

    @ModelData("category")
    String getCategory(@PathVariable("category") String category) {
        return category;
    }

    @ModelData("frontlet2Data")
    String frontlet2Data(@PathVariable("category") String category) {
        return "Frontlet2: " + category;
    }
}
