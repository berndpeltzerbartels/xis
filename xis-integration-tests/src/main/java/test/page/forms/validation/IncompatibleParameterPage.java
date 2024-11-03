package test.page.forms.validation;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;

@Page("/IncompatibleParameterPage.html")
class IncompatibleParameterPage {

    @FormData("test-object")
    IncompatibleParameterPageData data() {
        return new IncompatibleParameterPageData();
    }

    @Action("save")
    void action(@FormData("test-object") IncompatibleParameterPageData data) {
    }
}
