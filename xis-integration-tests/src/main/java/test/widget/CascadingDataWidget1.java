package test.widget;

import lombok.Getter;
import one.xis.ModelData;
import one.xis.Widget;

@Widget
class CascadingDataWidget1 {

    @Getter
    private String data1;

    @ModelData("data2")
    String data(@ModelData("data1") String data1) {
        this.data1 = data1;
        return "2";
    }
}
