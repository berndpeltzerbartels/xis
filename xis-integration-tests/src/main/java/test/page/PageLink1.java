package test.page;

import lombok.Getter;
import one.xis.ModelData;
import one.xis.Page;

@Getter
@Page("/pageLink1.html")
class PageLink1 {

    private int invocations;

    @ModelData("title")
    String title() {
        invocations++;
        return "PageLink1";
    }
}
