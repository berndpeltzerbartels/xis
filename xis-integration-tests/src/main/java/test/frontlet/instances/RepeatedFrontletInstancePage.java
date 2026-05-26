package test.frontlet.instances;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;

import java.util.List;

@Page("/repeated-frontlet-instance-page.html")
class RepeatedFrontletInstancePage {

    @ModelData
    List<Integer> items() {
        return List.of(1, 2);
    }

    @Action(value = "refreshItems", updateEventKeys = "item-updated")
    void refreshItems() {
    }
}
