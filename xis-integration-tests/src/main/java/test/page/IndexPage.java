package test.page;

import lombok.Getter;
import one.xis.Model;
import one.xis.Page;

@Getter
@Page("/index.html")
class IndexPage {

    private int invocations;

    @Model("title")
    String title() {
        invocations++;
        return "Index";
    }
}
