package test.page.core;

import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;
import one.xis.SharedValue;

@Page("/shared-value-action-refresh.html")
@RequiredArgsConstructor
class SharedValueActionRefreshPage {

    private final SharedValueActionRefreshService service;

    @SharedValue("value")
    String sharedValue() {
        return service.value();
    }

    @ModelData("value")
    String value(@SharedValue("value") String value) {
        return value;
    }

    @Action
    void update() {
        service.update("updated");
    }
}
