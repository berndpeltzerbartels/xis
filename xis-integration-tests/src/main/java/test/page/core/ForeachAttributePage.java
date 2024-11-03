package test.page.core;

import one.xis.ModelData;
import one.xis.Page;

import java.util.List;

@Page("/foreachAttribute.html")
class ForeachAttributePage {

    @ModelData("items")
    List<String> items() {
        return List.of("Item1", "Item2", "Item3");
    }
}
