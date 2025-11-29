package test.widget;

import one.xis.ModelData;
import one.xis.Widget;

@Widget
class ScrollTestWidget2 {
    
    @ModelData
    String message() {
        return "Second widget loaded!";
    }
}
