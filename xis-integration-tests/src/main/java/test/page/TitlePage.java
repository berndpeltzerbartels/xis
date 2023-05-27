package test.page;

import one.xis.Model;
import one.xis.Page;

@Page("/title.html")
class TitlePage {

    @Model("titleText")
    String title() {
        return "Hello ! I am the title";
    }

}
