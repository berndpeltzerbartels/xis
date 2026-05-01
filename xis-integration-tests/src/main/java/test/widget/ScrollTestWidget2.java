package test.widget;

import one.xis.ModelData;
import one.xis.Frontlet;

@Frontlet
class ScrollTestWidget2 {
    
    @ModelData
    String message() {
        return "Second widget loaded!";
    }
}
