package test.page.core;

import lombok.Getter;
import one.xis.ModelData;
import one.xis.Page;

@Getter
@Page("/pageButton2.html")
class PageButton2 {

    private int invocations;

    @ModelData("title")
    String title() {
        invocations++;
        return "PageButton2";
    }

    @ModelData("button2Content")
    String button2Content() {
        return "Go back to PageButton1";
    }
}
