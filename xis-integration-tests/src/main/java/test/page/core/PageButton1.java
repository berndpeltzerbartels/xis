package test.page.core;

import lombok.Getter;
import one.xis.ModelData;
import one.xis.Page;

@Getter
@Page("/pageButton1.html")
class PageButton1 {

    private int invocations;

    @ModelData("title")
    String title() {
        invocations++;
        return "PageButton1";
    }

    @ModelData("button1Content")
    String button1Content() {
        return "Go to PageButton2";
    }
}
