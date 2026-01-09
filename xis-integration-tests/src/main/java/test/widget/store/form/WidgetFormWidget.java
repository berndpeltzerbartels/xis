package test.widget.store.form;

import one.xis.Action;
import one.xis.FormData;
import one.xis.SessionStorage;
import one.xis.Widget;

@Widget
class WidgetFormWidget {

    @Action("increment-form")
    void incrementForm(@FormData("formData") FormDataObject formData, @SessionStorage("counter") Counter counter) {
        // Increment by the form value (default to 1 if empty)
        int increment = formData.incrementValue != null && !formData.incrementValue.isEmpty() ?
                Integer.parseInt(formData.incrementValue) : 1;
        counter.increment(increment);
    }

    public static class FormDataObject {
        public String incrementValue;
    }
}