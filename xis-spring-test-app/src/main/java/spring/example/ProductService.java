package spring.example;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Service
class ProductService {

    List<Product> allProducts() {
        return List.of();
    }

    List<Product> getByCategory(int categoryId) {
        var product = new Product();
        switch (categoryId) {
            case 1:

                product.setCategoryId(1);
                product.setTitle("Sofa - grau");
                product.setPrice(new BigDecimal("1200"));
                return List.of(product);

            case 2:

                product.setCategoryId(2);
                product.setTitle("Kühl-Gefrier-Kombination");
                product.setPrice(new BigDecimal("560"));
                return List.of(product);
            default:
                return Collections.emptyList();
        }


    }
}
