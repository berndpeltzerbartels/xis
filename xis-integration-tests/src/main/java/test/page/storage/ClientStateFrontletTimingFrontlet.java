package test.page.storage;

import one.xis.ClientState;
import one.xis.Frontlet;
import one.xis.ModelData;

@Frontlet
class ClientStateFrontletTimingFrontlet {

    @ModelData("frontletValue")
    int frontletValue(@ClientState("data") SessionStoragePageData data) {
        return data.getId();
    }
}
