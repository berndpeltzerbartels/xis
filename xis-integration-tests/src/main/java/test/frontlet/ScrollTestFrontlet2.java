package test.frontlet;

import one.xis.ModelData;
import one.xis.Frontlet;

@Frontlet
class ScrollTestFrontlet2 {
    
    @ModelData
    String message() {
        return "Second frontlet loaded!";
    }
}
