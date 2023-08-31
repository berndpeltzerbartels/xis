package test.widget;

import one.xis.ModelData;
import one.xis.Widget;

@Widget
class SimpleWidget {

    @ModelData("greeting")
    String getGreeting() {
        return "Huhu !";
    }
}
