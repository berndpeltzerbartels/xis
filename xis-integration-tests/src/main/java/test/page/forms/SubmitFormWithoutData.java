package test.page.forms;

import lombok.Getter;
import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;

@Page("/submitFormWithoutData.html")
public class SubmitFormWithoutData {

    @Getter
    private boolean invoked = false;

    @Action("save")
    void save(@FormData("formObject") SimpleObject simpleObject) {
        invoked = true;
    }
}
