package test.page.frontend;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Frontend;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.ToastLevel;

@Page("/frontend-parameter.html")
class FrontendParameterPage {

    @FormData("form")
    FrontendParameterFormData form() {
        var formData = new FrontendParameterFormData();
        formData.setValue("initial");
        return formData;
    }

    @ModelData("modelResult")
    String modelResult(Frontend frontend) {
        frontend.addModelData("modelSideResult", "side-loaded");
        return "main-loaded";
    }

    @Action("save")
    void save(@FormData("form") FrontendParameterFormData formData, Frontend frontend) {
        var returnedFormData = new FrontendParameterFormData();
        returnedFormData.setValue("server-" + formData.getValue());

        frontend.addModelData("result", "saved-" + formData.getValue())
                .addFormData("form", returnedFormData)
                .showToast("Saved " + formData.getValue(), ToastLevel.SUCCESS);
    }
}
