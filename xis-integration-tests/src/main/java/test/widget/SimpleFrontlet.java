package test.widget;

import one.xis.ModelData;
import one.xis.Frontlet;

@Frontlet
class SimpleFrontlet {

    @ModelData("greeting")
    String getGreeting() {
        return "Huhu !";
    }
}
