package test.page.storage;

import lombok.Getter;
import lombok.NonNull;
import one.xis.*;
import one.xis.context.XISInit;

import java.util.ArrayList;
import java.util.List;

@Getter
@Page("/client-storage.html")
class ClientStoragePage {

    private final List<String> invokedMethods = new ArrayList<>();
    private ClientStoragePageData clientStoragePageData;

    @XISInit
    void init() {
        this.clientStoragePageData = new ClientStoragePageData();
        this.clientStoragePageData.setId(100);
        this.clientStoragePageData.setValue("test");
    }


    @ClientStorage("data")
    ClientStoragePageData data() {
        invokedMethods.add("data");
        return this.clientStoragePageData;
    }

    @Action("link-action")
    @ClientStorage("data")
    ClientStoragePageData linkAction(@ClientStorage("data") ClientStoragePageData data) {
        invokedMethods.add("linkAction");
        this.clientStoragePageData = new ClientStoragePageData();
        this.clientStoragePageData.setId(data.getId() + 100);
        this.clientStoragePageData.setValue("test2");
        return this.clientStoragePageData;
    }

    @Action("form-action")
    @ClientStorage("data")
    ClientStoragePageData formAction(@NonNull @ClientStorage("data") ClientStoragePageData clientState, @NonNull @FormData("formData") ClientStoragePageData formData) {
        this.clientStoragePageData = clientState;
        invokedMethods.add("formAction");
        this.clientStoragePageData.setId(300);
        this.clientStoragePageData.setValue("test3");
        return this.clientStoragePageData;
    }

}
