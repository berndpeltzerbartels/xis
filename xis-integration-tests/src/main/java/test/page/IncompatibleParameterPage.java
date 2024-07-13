package test.page;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;

@Page("/IncompatibleParameterPage.html")
class IncompatibleParameterPage {

    @Action("save")
    void action(@ModelData("test-object") IncompatibleParameterPageData data) {
    }
}
