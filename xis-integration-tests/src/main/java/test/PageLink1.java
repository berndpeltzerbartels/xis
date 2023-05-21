package test;

import lombok.Getter;
import one.xis.Model;
import one.xis.Page;

@Getter
@Page("/pageLink1.html")
class PageLink1 {

    private int invocations;

    @Model("title")
    String title() {
        invocations++;
        return "PageLink1";
    }
}
