package test.widget;

import one.xis.LinkAction;
import one.xis.Model;
import one.xis.Widget;

@Widget
class ActionLinkWithTargetWidget2 {

    @Model("target")
    String target() {
        return "container1";
    }

    @LinkAction("test-action2")
    Class<?> action1() {
        return ActionLinkWithTargetWidget3.class;
    }
}
