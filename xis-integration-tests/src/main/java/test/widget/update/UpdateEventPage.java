package test.widget.update;

import one.xis.Action;
import one.xis.Page;

@Page("/updateEventPage.html")
class UpdateEventPage {

    @Action(value = "triggerUpdate", updateEventKeys = {"widget2-update"})
    void triggerUpdateFromPage() {
        // Action triggers update event, Widget2 should reload
        // Return Widget1 to stay on same widget in container1

    }
}
