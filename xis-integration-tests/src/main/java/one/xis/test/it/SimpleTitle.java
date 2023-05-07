package one.xis.test.it;

import one.xis.Model;
import one.xis.Page;

@Page("/simpleTitle.html")
class SimpleTitle {

    @Model("titleText")
    String title() {
        return "Hello !";
    }

}
