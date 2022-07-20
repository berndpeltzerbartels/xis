package spring.example;

import one.xis.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Widget
@Component
// TODO Widget's simple-name must be unique. Allow using an alias in @Widget
class ProductListWidget {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private BasketService basketService;

    @Model
    ProductListData createProductList() {
        var productListData = new ProductListData();
        productListData.setProductCategories(productCategoryService.allCategories());
        if (!productListData.getProductCategories().isEmpty()) {
            var category = productListData.getProductCategories().get(0);
            productListData.setCategoryId(category.getId());
            productListData.setProducts(productService.getByCategory(category.getId()));
        }
        return productListData;
    }

    @OnAction("categorySelected")
    void categorySelected(@Param long categoryId, @Model ProductListData productListData) {
        productListData.setCategoryId(categoryId);
        productListData.setProducts(productService.getByCategory(categoryId));
    }

    @OnAction("productToBasket")
    void productToBasket(@Param long productId, @ClientId String clientId) {
        basketService.addProduct(productId, clientId);
    }

}
