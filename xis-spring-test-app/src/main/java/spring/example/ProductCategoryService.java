package spring.example;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class ProductCategoryService {

    List<ProductCategory> allCategories() {
        return List.of();
    }
}
