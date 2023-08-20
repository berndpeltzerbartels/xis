package test.widget;


import one.xis.LinkAction;
import one.xis.Widget;
import test.page.IndexPage;

@Widget
class ActionRedirectsToNewPage {

    @LinkAction("test-action")
    Class<?> action() {
        return IndexPage.class;
    }
}
