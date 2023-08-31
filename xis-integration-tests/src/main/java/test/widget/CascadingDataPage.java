package test.widget;

import one.xis.ModelData;
import one.xis.Page;

@Page("/cascadingData.html")
class CascadingDataPage {

    @ModelData("data1")
    String data() {
        return "1";
    }

}
