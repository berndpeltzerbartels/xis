package spring.test;

import one.xis.Model;
import one.xis.Page;

import java.util.List;

@Page("/repeat.html")
class RepeatPage {

    @Model("items")
    List<RepeatPageItem> items() {
        return List.of(new RepeatPageItem(1, "title1"), new RepeatPageItem(2, "title2"), new RepeatPageItem(3, "title3"));
    }
}
