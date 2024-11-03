package test.page.core;

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

    @ModelData("link1-content")
    String link1Content() {
        return "Link to PageLink2";
    }
}
