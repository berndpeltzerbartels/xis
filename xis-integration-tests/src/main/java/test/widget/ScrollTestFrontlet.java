package test.widget;

import one.xis.ModelData;
import one.xis.Frontlet;

@Frontlet
class ScrollTestFrontlet {
    
    @ModelData
    String message() {
        return "Frontlet loaded successfully!";
    }
}
