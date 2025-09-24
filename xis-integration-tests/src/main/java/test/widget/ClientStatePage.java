package test.widget;

import lombok.Getter;
import lombok.NonNull;
import one.xis.Action;
import one.xis.ClientState;
import one.xis.FormData;
import one.xis.Page;
import one.xis.context.XISInit;
import test.page.core.ClientStatePageData;

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


    @ClientState("data")
    ClientStatePageData data() {
        invokedMethods.add("data");
        return this.clientStatePageData;
    }

    @Action("link-action")
    @ClientState("data")
    ClientStatePageData linkAction(@ClientState("data") ClientStatePageData data) {
        invokedMethods.add("linkAction");
        this.clientStatePageData = new ClientStatePageData();
        this.clientStatePageData.setId(data.getId() + 100);
        this.clientStatePageData.setValue("test2");
        return this.clientStatePageData;
    }

    @Action("form-action")
    @ClientState("data")
    ClientStatePageData formAction(@NonNull @ClientState("data") ClientStatePageData clientState, @NonNull @FormData("formData") ClientStatePageData formData) {
        this.clientStatePageData = clientState;
        invokedMethods.add("formAction");
        this.clientStatePageData.setId(300);
        this.clientStatePageData.setValue("test3");
        return this.clientStatePageData;
    }

}
