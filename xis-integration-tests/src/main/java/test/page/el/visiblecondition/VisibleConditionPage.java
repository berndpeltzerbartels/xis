package test.page.el.visiblecondition;

import lombok.RequiredArgsConstructor;
import one.xis.ModelData;
import one.xis.Page;

@Page("/visibleCondition.html")
@RequiredArgsConstructor
class VisibleConditionPage {

    private final VisibleConditionService service;

    @ModelData
    String data() {
        return service.getData();
    }
}
