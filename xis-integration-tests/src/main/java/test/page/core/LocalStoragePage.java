package test.page.core;

import lombok.Getter;
import one.xis.Action;
import one.xis.LocalStorage;
import one.xis.Page;

import java.util.ArrayList;
import java.util.List;

@Getter
@Page("/local-storage.html")
class LocalStoragePage {

    private final List<String> invokedMethods = new ArrayList<>();
    private LocalStoragePageData localStoragePageData;

    @LocalStorage("data")
    LocalStoragePageData data() {
        invokedMethods.add("data");
        this.localStoragePageData = new LocalStoragePageData();
        this.localStoragePageData.setId(500);
        this.localStoragePageData.setValue("localTest");
        return this.localStoragePageData;
    }

    @Action("update-action")
    @LocalStorage("data")
    LocalStoragePageData updateAction(@LocalStorage("data") LocalStoragePageData data) {
        invokedMethods.add("updateAction");
        this.localStoragePageData = new LocalStoragePageData();
        this.localStoragePageData.setId(data.getId() + 100);
        this.localStoragePageData.setValue("updatedLocalTest");
        return this.localStoragePageData;
    }
}