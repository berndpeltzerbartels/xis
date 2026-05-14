package test.page.modelcalls;

import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;

@Page("/model-call-frontlet-page.html")
@RequiredArgsConstructor
class ModelCallFrontletPage {
    private final ModelCallCounter counter;

    @ModelData("pageCalls")
    int pageCalls() {
        return counter.pageModelCalls();
    }

    @Action("touch")
    void touch() {
        counter.pageActionCalled();
    }
}
