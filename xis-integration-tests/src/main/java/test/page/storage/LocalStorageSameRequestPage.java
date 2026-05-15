package test.page.storage;

import one.xis.LocalStorage;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.SharedValue;

@Page("/local-storage-same-request.html")
class LocalStorageSameRequestPage {

    @ModelData
    @SharedValue("stateInitialized")
    int initializeState(@LocalStorage("data") SessionStoragePageData data) {
        data.setId(43);
        data.setValue("initialized");
        return 1;
    }

    @ModelData("valueFromSameRequest")
    int valueFromSameRequest(@SharedValue("stateInitialized") int ignored,
                             @LocalStorage("data") SessionStoragePageData data) {
        return data.getId();
    }
}
