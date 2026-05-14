package test.page.core.navigation;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.PathVariable;
import one.xis.QueryParameter;

@Page("/{a}/page2.html")
class NavigationPage2 {
    private String pathVariable;
    private String parameter;

    @ModelData("pathVariable")
    String getPathVariable(@PathVariable("a") String pathVariable) {
        return pathVariable;
    }

    @ModelData("queryParameter")
    String getQueryParameter(@QueryParameter("param") String parameter) {
        return parameter;
    }
}
