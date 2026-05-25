package test.page.modelcalls;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Frontlet;
import one.xis.ModelDataLoad;

@Frontlet
class FormDataLifecycleFrontlet {
    private int initialCalls;
    private int afterActionCalls;

    @FormData(value = "form", load = ModelDataLoad.INITIAL)
    FormDataLifecycleForm initialForm() {
        initialCalls++;
        return new FormDataLifecycleForm("frontlet-initial-" + initialCalls);
    }

    @FormData(value = "form", load = ModelDataLoad.AFTER_ACTION)
    FormDataLifecycleForm afterActionForm() {
        afterActionCalls++;
        return new FormDataLifecycleForm("frontlet-after-action-" + afterActionCalls);
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
