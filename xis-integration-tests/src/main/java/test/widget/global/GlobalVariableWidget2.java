package test.widget.global;

import lombok.Getter;
import one.xis.GlobalVariable;
import one.xis.Widget;

@Getter
@Widget
class GlobalVariableWidget2 {

    @GlobalVariable("globalWidgetValue")
    public String getSharedValue() {
        return "456";
    }
}