package test.frontlet.instances;

import one.xis.Action;
import one.xis.ActionParameter;
import one.xis.ModelData;
import one.xis.Page;

import java.util.List;

@Page("/repeated-action-frontlet-instance-page.html")
class RepeatedActionFrontletInstancePage {

    @ModelData
    List<Integer> items() {
        return List.of(1, 2);
    }

    @Action(value = "refreshItems", updateEventKeys = "action-item-updated")
    void refreshItems() {
    }

    @Action
    void selectItem(@ActionParameter("itemId") Integer itemId) {
    }
}
