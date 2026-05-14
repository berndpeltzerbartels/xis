package test.frontlet.update;

import one.xis.Action;
import one.xis.Page;

@Page("/updateEventPage.html")
class UpdateEventPage {

    @Action(value = "triggerUpdate", updateEventKeys = {"frontlet2-update"})
    void triggerUpdateFromPage() {
        // Action triggers update event, Frontlet2 should reload
        // Return Frontlet1 to stay on same frontlet in container1

    }
}
