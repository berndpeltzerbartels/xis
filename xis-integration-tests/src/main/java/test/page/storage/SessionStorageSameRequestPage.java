package test.page.storage;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.SessionStorage;
import one.xis.SharedValue;

@Page("/session-storage-same-request.html")
class SessionStorageSameRequestPage {

    @ModelData
    @SharedValue("stateInitialized")
    int initializeState(@SessionStorage("data") SessionStoragePageData data) {
        data.setId(42);
        data.setValue("initialized");
        return 1;
    }

    @ModelData("valueFromSameRequest")
    int valueFromSameRequest(@SharedValue("stateInitialized") int ignored,
                             @SessionStorage("data") SessionStoragePageData data) {
        return data.getId();
    }
}
