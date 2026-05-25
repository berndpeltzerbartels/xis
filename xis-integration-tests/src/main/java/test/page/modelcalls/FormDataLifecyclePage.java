package test.page.modelcalls;

import one.xis.Action;
import one.xis.FormData;
import one.xis.ModelDataLoad;
import one.xis.Page;

@Page("/form-data-lifecycle.html")
class FormDataLifecyclePage {
    private int initialCalls;
    private int afterActionCalls;

    @FormData(value = "form", load = ModelDataLoad.INITIAL)
    FormDataLifecycleForm initialForm() {
        initialCalls++;
        return new FormDataLifecycleForm("initial-" + initialCalls);
    }

    @FormData(value = "form", load = ModelDataLoad.AFTER_ACTION)
    FormDataLifecycleForm afterActionForm() {
        afterActionCalls++;
        return new FormDataLifecycleForm("after-action-" + afterActionCalls);
    }

    @Action
    void save(@FormData("form") FormDataLifecycleForm form) {
    }

    int getInitialCalls() {
        return initialCalls;
    }

    int getAfterActionCalls() {
        return afterActionCalls;
    }
}
