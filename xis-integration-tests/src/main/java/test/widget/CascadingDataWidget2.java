package test.widget;

import lombok.Getter;
import one.xis.ModelData;
import one.xis.Widget;

@Getter
@Widget
class CascadingDataWidget2 {

    @Getter
    private String data1;

    @Getter
    private String data2;

    @ModelData("data3")
    String data(@ModelData("data1") String data1, @ModelData("data2") String data2) {
        this.data1 = data1;
        this.data2 = data2;
        return "2";
    }
}
