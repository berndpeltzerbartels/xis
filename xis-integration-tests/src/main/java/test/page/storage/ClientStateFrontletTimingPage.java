package test.page.storage;

import one.xis.ClientState;
import one.xis.ModelData;
import one.xis.Page;

@Page("/client-state-frontlet-timing.html")
class ClientStateFrontletTimingPage {

    @ModelData
    int initializeState(@ClientState("data") SessionStoragePageData data) {
        data.setId(76);
        data.setValue("page");
        return 1;
    }
}
