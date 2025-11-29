package test.widget;

import one.xis.ModelData;
import one.xis.Widget;

@Widget
class ScrollTestWidget {
    
    @ModelData
    String message() {
        return "Widget loaded successfully!";
    }
}
