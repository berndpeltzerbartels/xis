package test.frontlet;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Frontlet;

@Frontlet
class ActionLinkWithTargetFrontlet1 {

    @ModelData("target")
    String target() {
        return "container2";
    }

    @Action("test-action1")
    Class<?> action1() {
        return ActionLinkWithTargetFrontlet2.class;
    }
}
