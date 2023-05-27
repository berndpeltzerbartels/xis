package test.widget;

import one.xis.Model;
import one.xis.Widget;

@Widget
class SimpleWidget {

    @Model("greeting")
    String getGreeting() {
        return "Huhu !";
    }
}
