package test.widget;


import one.xis.Action;
import one.xis.Widget;
import test.page.core.IndexPage;

@Widget
class ActionRedirectsToNewPage {

    @Action("test-action")
    Class<?> action() {
        return IndexPage.class;
    }
}
