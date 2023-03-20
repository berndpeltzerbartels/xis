package spring.example;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class ProductCategoryService {

    List<ProductCategory> allCategories() {
        var category1 = new ProductCategory();
        category1.setId(1);
        category1.setNumber(1);
        category1.setTitle("Wohnzimmer");
        var category2 = new ProductCategory();
        category2.setId(2);
        category2.setNumber(2);
        category2.setTitle("Küche");
        return List.of(category1, category2);
    }
}
