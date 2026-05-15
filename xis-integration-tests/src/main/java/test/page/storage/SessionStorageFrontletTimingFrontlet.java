package test.page.storage;

import one.xis.Frontlet;
import one.xis.ModelData;
import one.xis.SessionStorage;

@Frontlet
class SessionStorageFrontletTimingFrontlet {

    @ModelData("frontletValue")
    int frontletValue(@SessionStorage("data") SessionStoragePageData data) {
        return data.getId();
    }
}
