package test.widget;

import one.xis.Action;
import one.xis.Model;
import one.xis.Widget;

@Widget
class ActionLinkWithTargetWidget2 {

    @Model("target")
    String target() {
        return "container1";
    }

    @Action("test-action2")
    Class<?> action1() {
        return ActionLinkWithTargetWidget3.class;
    }
}
