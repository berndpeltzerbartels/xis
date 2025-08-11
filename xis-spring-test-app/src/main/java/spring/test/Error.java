package spring.test;

import one.xis.ModelData;
import one.xis.Page;

@Page("/error.html")
public class Error {

    @ModelData
    public String test() {
        if (System.currentTimeMillis() > 0) {
            throw new RuntimeException("Test error");
        }
        return "This is a test error page";
    }
}
