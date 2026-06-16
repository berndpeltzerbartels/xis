package test.page.forms;

import one.xis.ModelData;
import one.xis.Page;

@Page("/selectBoxIfTag.html")
class SelectBoxIfTagPage {

    @ModelData
    public boolean showOption() {
        return true;
    }
}
