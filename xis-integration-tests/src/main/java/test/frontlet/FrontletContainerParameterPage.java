package test.frontlet;

import one.xis.ModelData;
import one.xis.Page;

@Page("/frontletContainerParameterPage.html")
class FrontletContainerParameterPage {

    @ModelData("categoryValue")
    String categoryValue() {
        return "electronics";
    }

    @ModelData("sortValue")
    String sortValue() {
        return "price";
    }
}
