package test.frontlet.pathvariables;

import one.xis.ModelData;
import one.xis.PathVariable;
import one.xis.Frontlet;

@Frontlet(url = "/products/{category}.html", containerId = "container1")
class ProductFrontlet1 {

    @ModelData("category")
    String getCategory(@PathVariable("category") String category) {
        return category;
    }

    @ModelData("frontlet1Data")
    String getFrontlet1Data(@PathVariable("category") String category) {
        return "Frontlet1: " + category;
    }
}
