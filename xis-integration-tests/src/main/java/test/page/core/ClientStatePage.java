package test.page.core;

import lombok.Getter;
import lombok.NonNull;
import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;
import one.xis.SessionStorage;
import one.xis.context.XISInit;

import java.util.ArrayList;
import java.util.List;

@Getter
@Page("/client-state.html")
class ClientStatePage {

    private final List<String> invokedMethods = new ArrayList<>();
    private ClientStatePageData clientStatePageData;

    @XISInit
    void init() {
        this.clientStatePageData = new ClientStatePageData();
        this.clientStatePageData.setId(100);
        this.clientStatePageData.setValue("test");
    }


    @SessionStorage("data")
    ClientStatePageData data() {
        invokedMethods.add("data");
        return this.clientStatePageData;
    }

    @Action("link-action")
    @SessionStorage("data")
    ClientStatePageData linkAction(@SessionStorage("data") ClientStatePageData data) {
        invokedMethods.add("linkAction");
        this.clientStatePageData = new ClientStatePageData();
        this.clientStatePageData.setId(data.getId() + 100);
        this.clientStatePageData.setValue("test2");
        return this.clientStatePageData;
    }

    @Action("form-action")
    @SessionStorage("data")
    ClientStatePageData formAction(@NonNull @SessionStorage("data") ClientStatePageData clientState, @NonNull @FormData("formData") ClientStatePageData formData) {
        this.clientStatePageData = clientState;
        invokedMethods.add("formAction");
        this.clientStatePageData.setId(300);
        this.clientStatePageData.setValue("test3");
        return this.clientStatePageData;
    }

}
