package spring.test;

import one.xis.Model;
import one.xis.Page;

import java.util.ArrayList;
import java.util.List;

@Page("/pageWithRepeat.html")
class PageWithRepeat {

    @Model("items")
    List<PageWithRepeatItem> items(@Model("items") List<PageWithRepeatItem> items) {
        var list = new ArrayList<PageWithRepeatItem>();
        list.add(new PageWithRepeatItem(1, "title1"));
        list.add(new PageWithRepeatItem(2, "title2"));
        list.add(new PageWithRepeatItem(3, "title3"));
        return list;
    }
}
