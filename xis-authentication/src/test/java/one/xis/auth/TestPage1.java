package one.xis.auth;

import one.xis.ModelData;
import one.xis.Page;

@Page("/test.html")
class TestPage1 {
    @ModelData("message")
    String getMessage() {
        return "Success";
    }
}
