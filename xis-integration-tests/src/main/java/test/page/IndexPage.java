package test.page;

import lombok.Getter;
import one.xis.ModelData;
import one.xis.Page;

@Getter
@Page("/index.html")
public class IndexPage {

    private int invocations;

    @ModelData("title")
    String title() {
        invocations++;
        return "Index";
    }
}
