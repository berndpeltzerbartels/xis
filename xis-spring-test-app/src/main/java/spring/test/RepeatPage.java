package spring.test;

import one.xis.Model;
import one.xis.Page;

import java.util.ArrayList;
import java.util.List;

@Page("/repeat.html")
class RepeatPage {

    @Model("items")
    List<RepeatPageItem> items() {
        var list = new ArrayList<RepeatPageItem>();
        list.add(new RepeatPageItem(1, "title1"));
        list.add(new RepeatPageItem(2, "title2"));
        list.add(new RepeatPageItem(3, "title3"));
        return list;
    }
}
