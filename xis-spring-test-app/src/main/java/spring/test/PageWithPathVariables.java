package spring.test;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.PathVariable;

@Page("/{a}/pathVariables.html")
public class PageWithPathVariables {

    @ModelData
    String pathVariable(@PathVariable("a") String pathVariable) {
        return pathVariable;
    }
}
