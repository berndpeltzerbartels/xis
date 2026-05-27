package test.page.toastmessages;

import one.xis.Action;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.ToastLevel;
import one.xis.ToastMessages;

@Page("/toast-messages-parameter.html")
class ToastMessagesParameterPage {

    @FormData("form")
    ToastMessagesParameterFormData form() {
        var formData = new ToastMessagesParameterFormData();
        formData.setValue("initial");
        return formData;
    }

    @ModelData("modelResult")
    String modelResult() {
        return "main-loaded";
    }

    @Action("save")
    void save(@FormData("form") ToastMessagesParameterFormData formData, ToastMessages toastMessages) {
        toastMessages.show("Saved " + formData.getValue(), ToastLevel.SUCCESS);
    }
}
