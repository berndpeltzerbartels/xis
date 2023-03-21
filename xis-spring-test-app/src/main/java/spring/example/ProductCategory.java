package spring.example;


import lombok.Data;

import java.util.List;

@Data
class ProductCategory {
    private int id;
    private int number;
    private String title;
    private List<Product> products;
}
