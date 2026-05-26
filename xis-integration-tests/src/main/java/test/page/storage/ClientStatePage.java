package test.page.storage;

import lombok.Getter;
import one.xis.Action;
import one.xis.ClientState;
import one.xis.FormData;
import one.xis.Page;

@Getter
@Page("/client-state.html")
class ClientStatePage {

    private ClientStateStoreData storeData;

    @FormData("formData")
    ClientStateFormData formData() {
        return new ClientStateFormData("formInput");
    }

    @Action("link-action")
    void linkAction(@ClientState("storeData") ClientStateStoreData storeData) {
        storeData.addItem("linkAction");
        this.storeData = storeData;
    }

    @Action("form-action")
    void formAction(@FormData(("formData")) ClientStateFormData formData, @ClientState("storeData") ClientStateStoreData storeData) {
        storeData.addItem(formData.text());
        this.storeData = storeData;
    }

}
