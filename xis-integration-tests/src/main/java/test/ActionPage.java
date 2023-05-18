package test;

import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.Model;
import one.xis.Page;

import java.util.List;

@Page("/actionPage.html")
@RequiredArgsConstructor
class ActionPage {

    private final ActionPageService service;

    @Model("data-list")
    List<ActionPageData> actionPageData() {
        return service.getDataList();
    }

    @Action("test-action")
    Class<?> action(ActionPageData data) {
        service.update(data);
        return IndexPage.class;
    }
}


