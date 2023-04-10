package spring.test;

import one.xis.Model;
import one.xis.Page;
import one.xis.WelcomePage;

import java.util.ArrayList;
import java.util.List;

@WelcomePage
@Page("/index.html")
class Index {

    @Model("pages")
    List<PageLink> pages() {
        var list = new ArrayList<PageLink>();
        list.add(new PageLink("repeat", "Repeat", "/repeat.html"));
        list.add(new PageLink("repeatInsideRepeat", "RepeatInsideRepeat", "/repeatInsideRepeat.html"));
        return list;
    }
}
