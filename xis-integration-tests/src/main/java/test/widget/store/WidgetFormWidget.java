package test.widget.store;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Widget;

@Widget
class WidgetFormWidget {
    private int counter = 10;

    //@SessionStorage("counterValue")
    int count() {
        return counter;
    }

    @Action("increment-form")
    void incrementForm(@FormData("formData") FormDataObject formData) {
        // Increment by the form value (default to 1 if empty)
        int increment = formData.incrementValue != null && !formData.incrementValue.isEmpty() ?
                Integer.parseInt(formData.incrementValue) : 1;
        counter += increment;
    }

    public static class FormDataObject {
        public String incrementValue;
    }
}