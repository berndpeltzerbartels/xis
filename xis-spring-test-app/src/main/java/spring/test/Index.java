package spring.test;

import one.xis.Action;
import one.xis.Model;
import one.xis.Page;
import one.xis.WelcomePage;
import spring.example.ProductPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@WelcomePage
@Page("/index.html")
class Index {

    @Model("pages")
    List<PageLink> pages(@Model("pages") List<PageLink> pages) {
        var list = new ArrayList<PageLink>();
        list.add(new PageLink("ProductPage", "/product/details.html"));
        list.add(new PageLink("PageWithRepeat", "/pageWithRepeat.html"));
        return list;
    }

    @Action("randomPage")
    Class<?> randomPage() {
        return new Random().nextBoolean() ? ProductPage.class : PageWithRepeat.class;
    }
}
