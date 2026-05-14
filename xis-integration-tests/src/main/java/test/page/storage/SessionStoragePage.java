package test.page.storage;

import lombok.Getter;
import lombok.NonNull;
import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;
import one.xis.SessionStorage;
import one.xis.context.Init;

import java.util.ArrayList;
import java.util.List;

@Getter
@Page("/client-state.html")
class SessionStoragePage {

    private final List<String> invokedMethods = new ArrayList<>();
    private SessionStoragePageData sessionStoragePageData;

    @Init
    void init() {
        this.sessionStoragePageData = new SessionStoragePageData();
        this.sessionStoragePageData.setId(100);
        this.sessionStoragePageData.setValue("test");
    }


    // @SessionStorage("data")
    SessionStoragePageData data() {
        invokedMethods.add("data");
        return this.sessionStoragePageData;
    }

    @Action("link-action")
        // @SessionStorage("data")
    SessionStoragePageData linkAction(@SessionStorage("data") SessionStoragePageData data) {
        invokedMethods.add("linkAction");
        this.sessionStoragePageData = new SessionStoragePageData();
        this.sessionStoragePageData.setId(data.getId() + 100);
        this.sessionStoragePageData.setValue("test2");
        return this.sessionStoragePageData;
    }

    @Action("form-action")
        //@SessionStorage("data")
    SessionStoragePageData formAction(@NonNull @SessionStorage("data") SessionStoragePageData clientState, @NonNull @FormData("formData") SessionStoragePageData formData) {
        this.sessionStoragePageData = clientState;
        invokedMethods.add("formAction");
        this.sessionStoragePageData.setId(300);
        this.sessionStoragePageData.setValue("test3");
        return this.sessionStoragePageData;
    }

}
