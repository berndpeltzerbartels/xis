package test.page.forms;

import one.xis.Action;
import one.xis.FormData;
import one.xis.ModelData;
import one.xis.Page;

@Page("/simpleRecordForm/new.html")
class SimpleRecordForm {

    private SimpleRecord savedRecord;

    @FormData("recordForm")
    SimpleRecord recordForm() {
        return new SimpleRecord(null, "New User", "John", "Doe");
    }

    @Action("save")
    void save(@FormData("recordForm") SimpleRecord recordForm) {
        this.savedRecord = recordForm;
    }

    @ModelData("savedRecord")
    SimpleRecord savedRecord() {
        return savedRecord;
    }
}
