package test.page.storage;

import lombok.Getter;
import one.xis.Action;
import one.xis.ClientStorage;
import one.xis.FormData;
import one.xis.Page;

@Getter
@Page("/client-storage.html")
class ClientStoragePage {

    private ClientStorageStoreData storeData;

    @FormData("formData")
    ClientStorageFormData formData() {
        return new ClientStorageFormData("formInput");
    }

    @Action("link-action")
    void linkAction(@ClientStorage("storeData") ClientStorageStoreData storeData) {
        storeData.addItem("linkAction");
        this.storeData = storeData;
    }

    @Action("form-action")
    void formAction(@FormData(("formData")) ClientStorageFormData formData, @ClientStorage("storeData") ClientStorageStoreData storeData) {
        storeData.addItem(formData.text());
        this.storeData = storeData;
    }

}
