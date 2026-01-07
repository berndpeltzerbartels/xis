package test.page.binding.localstorage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.Action;
import one.xis.ActionParameter;
import one.xis.LocalStorage;
import one.xis.Widget;

import java.util.ArrayList;
import java.util.List;

@Widget
public class ProductWidget {

    private List<Product> products = new ArrayList<>();

    ProductWidget() {
        products.add(new Product("Product 1", 10.99));
        products.add(new Product("Product 2", 25.50));
        products.add(new Product("Product 3", 15.75));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Product {
        private String name;
        private double price;
    }

    @Data
    @NoArgsConstructor
    public static class ShoppingCard {
        private List<Product> products = new ArrayList<>();
    }


    @Action("addProduct")
    ShoppingCard addProduct(@LocalStorage("shoppingCard") ShoppingCard card, @ActionParameter("index") int productIndex) {
        card.getProducts().add(products.get(productIndex));
        return card;
    }
}
