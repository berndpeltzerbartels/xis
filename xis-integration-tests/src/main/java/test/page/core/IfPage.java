package test.page.core;

import lombok.Setter;
import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;

@Setter
@Page("/ifPage.html")
class IfPage {
    private boolean condition1;
    private boolean condition2;

    @ModelData("condition1")
    boolean condition1() {
        return condition1;
    }

    @ModelData("condition2")
    boolean condition2() {
        return condition2;
    }

    @Action("switchCondition1")
    void switchCondition1() {
        condition1 = !condition1;
    }

    @Action("switchCondition2")
    void switchCondition2() {
        condition2 = !condition2;
    }
}
