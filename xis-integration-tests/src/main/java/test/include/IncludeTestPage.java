package test.include;

import one.xis.ModelData;
import one.xis.Page;

@Page("/include-test.html")
public class IncludeTestPage {

    @ModelData("message")
    public String getMessage() {
        return "Hello from Page";
    }
}
