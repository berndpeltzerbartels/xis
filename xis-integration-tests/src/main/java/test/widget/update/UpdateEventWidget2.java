package test.widget.update;

import one.xis.ModelData;
import one.xis.RefreshOnUpdateEvents;
import one.xis.Frontlet;

@Frontlet
@RefreshOnUpdateEvents("widget2-update")
class UpdateEventWidget2 {

    private int loadCounter = 0;

    @ModelData("loadCount")
    int getLoadCount() {
        return ++loadCounter;
    }
}
