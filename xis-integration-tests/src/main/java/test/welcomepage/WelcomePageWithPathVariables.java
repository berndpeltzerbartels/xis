package test.welcomepage;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.PathVariable;
import one.xis.WelcomePage;

@Page("/category/{name}.html")
@WelcomePage("/category/electronics.html")
public class WelcomePageWithPathVariables {

    @ModelData("categoryName")
    String getCategoryName(@PathVariable("name") String name) {
        return name;
    }

    @ModelData("title")
    String getTitle(@PathVariable("name") String name) {
        return "Category: " + name;
    }
}
