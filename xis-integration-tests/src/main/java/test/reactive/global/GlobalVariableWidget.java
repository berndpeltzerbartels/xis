package test.reactive.global;

import lombok.Getter;
import one.xis.GlobalVariable;
import one.xis.Widget;

@Getter
@Widget
class GlobalVariableWidget {

    @GlobalVariable("globalWidgetValue")
    public String getSharedValue() {
        return "456";
    }
}