package test.page.storage;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.SessionStorage;

@Page("/session-storage-frontlet-timing.html")
class SessionStorageFrontletTimingPage {

    @ModelData
    int initializeState(@SessionStorage("data") SessionStoragePageData data) {
        data.setId(74);
        data.setValue("page");
        return 1;
    }
}
