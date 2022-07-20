package spring.example;

import lombok.Data;

import java.util.List;

@Data
class ProductListData {

    private List<Product> products;
    private List<ProductCategory> productCategories;
    private Long categoryId;
}
