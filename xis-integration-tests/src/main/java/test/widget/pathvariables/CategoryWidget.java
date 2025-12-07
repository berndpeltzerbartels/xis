package test.widget.pathvariables;

import one.xis.ModelData;
import one.xis.PathVariable;
import one.xis.Widget;

@Widget(id = "CategoryWidget", url = "/category/{categoryId}", title = "Category")
class CategoryWidget {

    @ModelData("categoryId")
    String getCategoryId(@PathVariable("categoryId") String categoryId) {
        return categoryId;
    }

    @ModelData("categoryName")
    String getCategoryName(@PathVariable("categoryId") String categoryId) {
        return "Category: " + categoryId;
    }
}
