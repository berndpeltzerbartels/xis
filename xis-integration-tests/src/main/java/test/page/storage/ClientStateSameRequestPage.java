package test.page.storage;

import one.xis.ClientState;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.SharedValue;

@Page("/client-state-same-request.html")
class ClientStateSameRequestPage {

    @ModelData
    @SharedValue("stateInitialized")
    int initializeState(@ClientState("data") SessionStoragePageData data) {
        data.setId(44);
        data.setValue("initialized");
        return 1;
    }

    @ModelData("valueFromSameRequest")
    int valueFromSameRequest(@SharedValue("stateInitialized") int ignored,
                             @ClientState("data") SessionStoragePageData data) {
        return data.getId();
    }
}
