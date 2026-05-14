package test.frontlet.update;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Frontlet;

@Frontlet
class UpdateEventFrontlet1 {

    private int loadCounter = 0;

    @ModelData("loadCount")
    int getLoadCount() {
        return ++loadCounter;
    }

    @Action(value = "triggerFrontlet2Update", updateEventKeys = {"frontlet2-update"})
    void triggerUpdate() {
        // Action that emits update event for Frontlet2
    }
}
