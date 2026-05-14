package test.page.el.expression;

import lombok.AllArgsConstructor;
import lombok.Getter;
import one.xis.ModelData;
import one.xis.Page;

import java.util.List;
import java.util.Map;

@Page("/expressionLanguage.html")
class ExpressionLanguagePage {

    @Getter
    @AllArgsConstructor
    static class Item {
        private final String id;
        private final String label;
    }

    @ModelData("lookup")
    Map<String, Item> lookup() {
        return Map.of("first_item", new Item("first", "First"));
    }

    @ModelData("prefix")
    String prefix() {
        return "first";
    }

    @ModelData("items")
    List<String> items() {
        return List.of("alpha", "beta", "gamma");
    }

    @ModelData("products")
    List<Item> products() {
        return List.of(
                new Item("p1", "Keyboard"),
                new Item("p2", "Mouse"),
                new Item("p3", "Monitor")
        );
    }

    @ModelData("enabled")
    boolean enabled() {
        return true;
    }

    @ModelData("emptyValue")
    String emptyValue() {
        return "";
    }
}
