package test.page;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.Model;
import one.xis.Page;

@Page("/actionPage.html")
@RequiredArgsConstructor
class ActionLinkPage {

    private final ActionPageService service;

    @Model("data")
    ActionLinkPageData data() {
        return service.getData();
    }

    @Model("action3")
    String getAction3() {
        return "test-action3";
    }

    @Action("test-action1")
    void action1(@Model("data") ActionLinkPageData data) {
        service.update(data);
    }

    @Action("test-action2")
    Class<?> action2(@Model("data") ActionLinkPageData data) {
        service.update(data);
        return ActionLinkPage.class;
    }

    @Action("test-action3")
    Class<?> action3(@Model("data") ActionLinkPageData data, @NonNull @Model("action3") String action3) {
        service.update(data);
        return IndexPage.class;
    }


}


