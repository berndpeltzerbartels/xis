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

        switch (categoryId) {
            case 1:
                var product = new Product();
                product.setCategoryId(1);
                product.setTitle("Sofa - grau");
                product.setPrice(new BigDecimal("1200"));
                var product1 = new Product();
                product1.setCategoryId(1);
                product1.setTitle("Vitrinenschrank");
                product1.setPrice(new BigDecimal("800"));
                return List.of(product, product1);

            case 2:
                var product2 = new Product();
                product2.setCategoryId(2);
                product2.setTitle("Sitzgruppe");
                product2.setPrice(new BigDecimal("750"));
                var product3 = new Product();
                product3.setCategoryId(2);
                product3.setTitle("Kühl-Gefrier-Kombination");
                product3.setPrice(new BigDecimal("560"));
                return List.of(product2, product3);
            default:
                return Collections.emptyList();
        }


    }
}
