package test.page.storage;

import one.xis.LocalStorage;
import one.xis.ModelData;
import one.xis.Page;

@Page("/local-storage-frontlet-timing.html")
class LocalStorageFrontletTimingPage {

    @ModelData
    int initializeState(@LocalStorage("data") SessionStoragePageData data) {
        data.setId(75);
        data.setValue("page");
        return 1;
    }
}
