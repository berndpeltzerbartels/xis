package test.page.storage;

import one.xis.ClientStorage;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.SharedValue;

@Page("/client-storage-same-request.html")
class ClientStorageSameRequestPage {

    @ModelData
    @SharedValue("stateInitialized")
    int initializeState(@ClientStorage("data") SessionStoragePageData data) {
        data.setId(44);
        data.setValue("initialized");
        return 1;
    }

    @ModelData("valueFromSameRequest")
    int valueFromSameRequest(@SharedValue("stateInitialized") int ignored,
                             @ClientStorage("data") SessionStoragePageData data) {
        return data.getId();
    }
}
