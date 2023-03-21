package spring.example;

import lombok.Data;

import java.math.BigDecimal;

@Data
class Product {
    private int id;
    private String title;
    private BigDecimal price;
    private long categoryId;
}
