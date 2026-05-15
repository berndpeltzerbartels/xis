package test.page.storage;

import one.xis.ClientStorage;
import one.xis.Frontlet;
import one.xis.ModelData;

@Frontlet
class ClientStorageFrontletTimingFrontlet {

    @ModelData("frontletValue")
    int frontletValue(@ClientStorage("data") SessionStoragePageData data) {
        return data.getId();
    }
}
