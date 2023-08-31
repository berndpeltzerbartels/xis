package test.widget;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Widget;

@Widget
class ActionLinkWithTargetWidget1 {

    @ModelData("target")
    String target() {
        return "container2";
    }

    @Action("test-action1")
    Class<?> action1() {
        return ActionLinkWithTargetWidget2.class;
    }
}
