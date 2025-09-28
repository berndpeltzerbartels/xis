package test.widget.parentdata;

import one.xis.ModelData;
import one.xis.Page;

@Page("/parentdata.html")
public class TestPage {
    @ModelData("parentValue")
    public String getParentValue() {
        return "parent123";
    }
}
