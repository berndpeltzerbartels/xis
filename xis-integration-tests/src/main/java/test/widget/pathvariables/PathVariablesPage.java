package test.widget.pathvariables;

import one.xis.ModelData;
import one.xis.Page;

@Page("/products/{category}.html")
class PathVariablesPage {

    @ModelData("title")
    String getTitle() {
        return "Products Page";
    }
}
