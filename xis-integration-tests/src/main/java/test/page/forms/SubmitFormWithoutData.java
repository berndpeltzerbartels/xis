package test.page.forms;

import lombok.Getter;
import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;

@Getter
@Page("/submitFormWithoutData.html")
public class SubmitFormWithoutData {
    
    private boolean invoked = false;

    @Action("save")
    void save(@FormData("formObject") SimpleObject simpleObject) {
        invoked = true;
    }
}
