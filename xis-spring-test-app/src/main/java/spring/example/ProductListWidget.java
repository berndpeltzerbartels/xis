package spring.example;

import one.xis.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Widget
@Component
class ProductListWidget {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductCategoryService productCategoryService;

    @Autowired
    private BasketService basketService;

    @Model("categories")
    List<ProductCategory> createProductList() {
        var categories = productCategoryService.allCategories();
        for (var category : categories) {
            category.setProducts(productService.getByCategory(category.getId()));
        }
        return categories;
    }


    @Action("categorySelected")
    void categorySelected(@PathElement("categoryId") int categoryId, @Model("productListData") ProductListData productListData) {
        productListData.setCategoryId(categoryId);
        productListData.setProducts(productService.getByCategory(categoryId));
    }

    @Action("productToBasket")
    void productToBasket(@PathElement("productId") long productId, @ClientId String clientId) {
        basketService.addProduct(productId, clientId);
    }

}
