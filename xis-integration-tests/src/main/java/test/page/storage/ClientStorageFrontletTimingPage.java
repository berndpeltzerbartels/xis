package test.page.storage;

import one.xis.ClientStorage;
import one.xis.ModelData;
import one.xis.Page;

@Page("/client-storage-frontlet-timing.html")
class ClientStorageFrontletTimingPage {

    @ModelData
    int initializeState(@ClientStorage("data") SessionStoragePageData data) {
        data.setId(76);
        data.setValue("page");
        return 1;
    }
}
