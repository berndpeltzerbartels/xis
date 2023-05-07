package test;

import one.xis.Model;
import one.xis.Page;

import java.util.List;

@Page("/foreachAttribute.html")
class ForeachAttributePage {

    @Model("items")
    List<String> items() {
        return List.of("Item1", "Item2", "Item3");
    }
}
