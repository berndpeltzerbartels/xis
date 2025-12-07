package test.widget.pathvariables;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.PathVariable;
import one.xis.Widget;
import one.xis.WidgetResponse;

@Widget(id = "ProductWidget", url = "/product/{productId}.html", title = "Product Details")
class ProductWidget {

    @ModelData("productId")
    String getProductId(@PathVariable("productId") String productId) {
        return productId;
    }

    @ModelData("productName")
    String getProductName(@PathVariable("productId") String productId) {
        return "Product " + productId;
    }

    @Action
    WidgetResponse loadCategory(@PathVariable("productId") String currentProductId) {
        // Navigate to category widget with path variable
        return WidgetResponse.ofPathVariable(CategoryWidget.class, "categoryId", "electronics");
    }
}
