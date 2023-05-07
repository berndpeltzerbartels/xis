package one.xis.test.it;

import one.xis.Model;
import one.xis.Page;

import java.util.List;

@Page("/simpleLoopRepeatAttr.html")
class SimpleLoopRepeatAttr {

    @Model("items")
    List<String> items() {
        return List.of("Item1", "Item2", "Item3");
    }
}
