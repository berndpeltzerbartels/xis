package spring.example;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
class ProductService {

    List<Product> allProducts() {
        return List.of();
    }

    List<Product> getByCategory(long categoryId) {
        return List.of();
    }
}
