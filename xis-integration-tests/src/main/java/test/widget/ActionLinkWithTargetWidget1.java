package test.widget;

import one.xis.Action;
import one.xis.Model;
import one.xis.Widget;

@Widget
class ActionLinkWithTargetWidget1 {

    @Model("target")
    String target() {
        return "container2";
    }

    @Action("test-action1")
    Class<?> action1() {
        return ActionLinkWithTargetWidget2.class;
    }
}
