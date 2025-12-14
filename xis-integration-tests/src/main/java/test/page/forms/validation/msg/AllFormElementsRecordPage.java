package test.page.forms.validation.msg;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;

@Page("/allFormElementsRecord.html")
class AllFormElementsRecordPage {

    @Action
    void save(@FormData("formObject") AllFormElementsRecordModel formObject) {
        // This method is intentionally left empty to simulate a save action
        // The validation messages will be triggered by the mandatory fields in the model
    }
}
