package test.page.core;

import one.xis.Action;
import one.xis.ActionParameter;
import one.xis.ModelData;
import one.xis.Page;

@Page("/actionAndModel.html")
public class ActionAndModel {

    /**
     * An action method that processes the input and simultaneously provides the result
     * as model data using the @ModelData annotation.
     */
    @Action("test-action-model")
    @ModelData("modelActionData")
    public String modelAction(@ActionParameter("value") String value) {
        return "Processed: " + value;
    }
}