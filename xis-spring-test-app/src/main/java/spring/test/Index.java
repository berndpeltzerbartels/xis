package spring.test;

import one.xis.Model;
import one.xis.Page;
import one.xis.WelcomePage;

import java.util.List;

@WelcomePage
@Page("/index.html")
class Index {

    @Model("pages")
    List<PageLink> pages() {
        return List.of(new PageLink("repeat", "Repeat", "/repeat.html"), new PageLink("repeatInsideRepeat", "RepeatInsideRepeat", "/repeatInsideRepeat.html"));
    }
}
