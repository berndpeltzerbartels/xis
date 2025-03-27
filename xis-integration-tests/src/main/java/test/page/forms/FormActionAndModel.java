package test.page.forms;

import one.xis.Action;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.Page;

@Page("/formActionAndModel.html")
class FormActionAndModel {

    @FormData("formObject")
    FormActionAndModelObject formActionAndModelObject() {
        var formObject = new FormActionAndModelObject();
        formObject.setProperty1("defaultValue");
        return formObject;
    }

    /**
     * An action method that processes the input and simultaneously provides the result
     * as model data using the @ModelData annotation.
     */
    @Action("save")
    @ModelData("result")
    @FormData("formObject")
    public String modelAction(@FormData("formObject") FormActionAndModelObject formObject) {
        return "Processed: " + formObject.getProperty1();
    }


}
