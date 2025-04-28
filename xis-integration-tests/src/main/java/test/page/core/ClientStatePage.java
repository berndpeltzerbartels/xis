package test.page.core;

import lombok.Getter;
import one.xis.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Page("/client-state.html")
class ClientStatePage {

    private final List<String> invokateddMethods = new ArrayList<>();
    private ClientStatePageData pageData;

    @ModelData("clientState")
    @ClientState("clientState")
    ClientStatePageData data() {
        invokateddMethods.add("data");
        this.pageData = new ClientStatePageData(100, "test");
        return pageData;
    }

    @Action("link-action")
    @ClientState("clientState")
    ClientStatePageData linkAction(@ClientState("clientState") ClientStatePageData clientState) {
        invokateddMethods.add("linkAction");
        this.pageData = clientState;
        this.pageData.setId(200);
        this.pageData.setValue("test2");
        return this.pageData;
    }

    @Action("form-action")
    void formAction(@ClientState("clientState") ClientStatePageData clientState, @FormData("formData") Object formData) {
        this.pageData = clientState;
        invokateddMethods.add("formAction");
    }

}
