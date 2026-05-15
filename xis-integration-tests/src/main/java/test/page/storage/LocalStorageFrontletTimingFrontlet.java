package test.page.storage;

import one.xis.Frontlet;
import one.xis.LocalStorage;
import one.xis.ModelData;

@Frontlet
class LocalStorageFrontletTimingFrontlet {

    @ModelData("frontletValue")
    int frontletValue(@LocalStorage("data") SessionStoragePageData data) {
        return data.getId();
    }
}
