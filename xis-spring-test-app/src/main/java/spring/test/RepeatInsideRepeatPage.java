package spring.test;

import one.xis.Model;
import one.xis.Page;

import java.util.Collections;
import java.util.List;

@Page("/repeatInsideRepeat.html")
class RepeatInsideRepeatPage {

    @Model("items")
    List<RepeatInsideRepeatPageItem> items() {
        var subItem1 = new RepeatInsideRepeatPageSubItem("subItem1");
        var subItem2 = new RepeatInsideRepeatPageSubItem("subItem2");
        var subItem3 = new RepeatInsideRepeatPageSubItem("subItem3");

        var item1 = new RepeatInsideRepeatPageItem("title1", List.of(subItem1));
        var item2 = new RepeatInsideRepeatPageItem("title2", List.of(subItem2, subItem3));
        var item3 = new RepeatInsideRepeatPageItem("title3", Collections.emptyList());

        return List.of(item1, item2, item3);

    }
}
