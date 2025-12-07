package test.widget.pathvariables;

import one.xis.ModelData;
import one.xis.Page;

@Page("/product/{productId}.html")
class PathVariablesPage {

    @ModelData("title")
    String getTitle() {
        return "Product Page";
    }
}
