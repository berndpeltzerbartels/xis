package test.frontlet.update;

import one.xis.ModelData;
import one.xis.RefreshOnUpdateEvents;
import one.xis.Frontlet;

@Frontlet
@RefreshOnUpdateEvents("frontlet2-update")
class UpdateEventFrontlet2 {

    private int loadCounter = 0;

    @ModelData("loadCount")
    int getLoadCount() {
        return ++loadCounter;
    }
}
