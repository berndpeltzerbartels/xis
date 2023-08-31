package test.page;

import one.xis.ModelData;
import one.xis.Page;

import java.util.List;

@Page("/foreachTag.html")
class ForeachTagPage {

    @ModelData("items")
    List<String> items() {
        return List.of("Item1", "Item2", "Item3");
    }
}
