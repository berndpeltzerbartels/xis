package test.page.core;

import one.xis.ModelData;
import one.xis.Page;

@Page("/foreachWithExprArray.html")
class ForeachWithExprArray {

    @ModelData("arrayName1")
    String name1() {
        return "list1";
    }

    @ModelData("arrayName2")
    String name2() {
        return "list2";
    }

    @ModelData("list1")
    int[] list1() {
        return new int[]{1, 2, 3};
    }

    @ModelData("list2")
    int[] list2() {
        return new int[]{4, 5, 6};
    }
}
