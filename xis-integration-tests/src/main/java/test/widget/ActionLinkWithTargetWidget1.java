package test.widget;

import one.xis.LinkAction;
import one.xis.Model;
import one.xis.Widget;

@Widget
class ActionLinkWithTargetWidget1 {

    @Model("target")
    String target() {
        return "container2";
    }

    @LinkAction("test-action1")
    Class<?> action1() {
        return ActionLinkWithTargetWidget2.class;
    }
}
