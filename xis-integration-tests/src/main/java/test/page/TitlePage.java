package test.page;

import one.xis.ModelData;
import one.xis.Page;

@Page("/title.html")
class TitlePage {

    @ModelData("titleText")
    String title() {
        return "Hello ! I am the title";
    }

}
