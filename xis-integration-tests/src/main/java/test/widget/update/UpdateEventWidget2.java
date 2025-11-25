package test.widget.update;

import one.xis.ModelData;
import one.xis.RefreshOnUpdateEvents;
import one.xis.Widget;

@Widget
@RefreshOnUpdateEvents("widget2-update")
class UpdateEventWidget2 {

    private int loadCounter = 0;

    @ModelData("loadCount")
    int getLoadCount() {
        return ++loadCounter;
    }
}
