package test.page;

import one.xis.Model;
import one.xis.Page;

@Page("/foreachWithExprArray.html")
class ForeachWithExprArray {

    @Model("arrayName1")
    String name1() {
        return "list1";
    }

    @Model("arrayName2")
    String name2() {
        return "list2";
    }

    @Model("list1")
    int[] list1() {
        return new int[]{1, 2, 3};
    }

    @Model("list2")
    int[] list2() {
        return new int[]{4, 5, 6};
    }
}
