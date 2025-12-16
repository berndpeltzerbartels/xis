package test.page.core.navigation;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;

@Page("/page1.html")
class NavigationPage1 {

    @Action
    Class<?> action1() {
        return NavigationPage3.class;
    }

    @Action
    Class<?> action3(@FormData("formData") NavigationPageFormData formData) {
        return NavigationPage3.class;
    }

    @Action
    String action2() {
        return "/xyz/page2.html?param=123";
    }
}
