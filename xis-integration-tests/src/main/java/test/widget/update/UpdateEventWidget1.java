package test.widget.update;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Widget;

@Widget
class UpdateEventWidget1 {

    private int loadCounter = 0;

    @ModelData("loadCount")
    int getLoadCount() {
        return ++loadCounter;
    }

    @Action(value = "triggerWidget2Update", updateEventKeys = {"widget2-update"})
    void triggerUpdate() {
        // Action that emits update event for Widget2
    }
}
