package test.page;

import lombok.Getter;
import one.xis.ModelData;
import one.xis.Page;

@Getter
@Page("/pageLink2.html")
class PageLink2 {

    private int invocations;

    @ModelData("title")
    String title() {
        invocations++;
        return "PageLink2";
    }
}
