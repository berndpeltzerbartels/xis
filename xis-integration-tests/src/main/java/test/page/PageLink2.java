package test.page;

import lombok.Getter;
import one.xis.Model;
import one.xis.Page;

@Getter
@Page("/pageLink2.html")
class PageLink2 {

    private int invocations;

    @Model("title")
    String title() {
        invocations++;
        return "PageLink2";
    }
}
