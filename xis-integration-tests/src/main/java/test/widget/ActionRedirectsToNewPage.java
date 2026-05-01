package test.widget;


import one.xis.Action;
import one.xis.Frontlet;
import test.page.core.IndexPage;

@Frontlet
class ActionRedirectsToNewPage {

    @Action("test-action")
    Class<?> action() {
        return IndexPage.class;
    }
}
