package xisapp;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.WelcomePage;

@WelcomePage
@Page("/default-develop-index.html")
class IndexPage {

    @ModelData
    String testMessage() {
        return "Hello from XIS Boot!";
    }

    @Action("throw-exception")
    void throwTestException() {
        throw new RuntimeException("This is a test exception to verify exception handling in xis-boot");
    }

    @Action("throw-npe")
    void throwNullPointerException() {
        String nullString = null;
        // This will throw a NullPointerException
        int length = nullString.length();
    }
}
