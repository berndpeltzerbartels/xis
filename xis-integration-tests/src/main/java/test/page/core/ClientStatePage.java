package test.page.core;

import lombok.Getter;
import lombok.NonNull;
import one.xis.Action;
import one.xis.ClientState;
import one.xis.FormData;
import one.xis.Page;

import java.util.ArrayList;
import java.util.List;

@Getter
@Page("/client-state.html")
class ClientStatePage {

    private final List<String> invokateddMethods = new ArrayList<>();
    private ClientStatePageData clientStatePageData;

    @Action("link-action")
    @ClientState("data")
    ClientStatePageData linkAction() {
        invokateddMethods.add("linkAction");
        this.clientStatePageData = new ClientStatePageData();
        this.clientStatePageData.setId(200);
        this.clientStatePageData.setValue("test2");
        return this.clientStatePageData;
    }

    @Action("form-action")
    @ClientState("data")
    ClientStatePageData formAction(@NonNull @ClientState("data") ClientStatePageData clientState, @NonNull @FormData("formData") ClientStatePageData formData) {
        this.clientStatePageData = clientState;
        invokateddMethods.add("formAction");
        this.clientStatePageData.setId(300);
        this.clientStatePageData.setValue("test3");
        return this.clientStatePageData;
    }

}
