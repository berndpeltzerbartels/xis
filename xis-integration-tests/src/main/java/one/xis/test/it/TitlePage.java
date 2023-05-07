package one.xis.test.it;

import one.xis.Model;
import one.xis.Page;

@Page("/title.html")
class TitlePage {

    @Model("titleText")
    String title() {
        return "Hello !";
    }

}
