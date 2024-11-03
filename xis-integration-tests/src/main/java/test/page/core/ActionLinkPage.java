package test.page.core;

import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.ActionParameter;
import one.xis.ModelData;
import one.xis.Page;

@Page("/actionPage.html")
@RequiredArgsConstructor
class ActionLinkPage {

    private final ActionLinkPageService service;

    @ModelData("actionLinkData")
    ActionLinkPageData data() {
        return service.getData();
    }

    @ModelData("action3")
    String getAction3() {
        return "test-action3";
    }

    @Action("test-action1")
    void action1(@ActionParameter("value") String value) {
        service.update(new ActionLinkPageData(123, value));
    }

    @Action("test-action2")
    Class<?> action2(@ActionParameter("id") int id, @ActionParameter("value") String value) {
        service.update(new ActionLinkPageData(id, value));
        return ActionLinkPage.class;
    }

    @Action("test-action3")
    Class<?> action3(@ActionParameter("id") int id, @ActionParameter("value3") String value) {
        service.update(new ActionLinkPageData(id, value));
        return IndexPage.class;
    }


}


