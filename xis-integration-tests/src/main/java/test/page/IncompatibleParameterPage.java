package test.page;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;

@Page("/IncompatibleParameterPage.html")
class IncompatibleParameterPage {

    @Action("save")
    void action(@FormData("test-object") IncompatibleParameterPageData data) {
    }
}
