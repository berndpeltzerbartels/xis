package test.widget;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Widget;

@Widget
class ActionLinkWithTargetWidget2 {

    @ModelData("target")
    String target() {
        return "container1";
    }

    @Action("test-action2")
    Class<?> action1() {
        return ActionLinkWithTargetWidget3.class;
    }
}
