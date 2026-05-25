package test.page.modelcalls;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;

@Page("/action-form-data-lifecycle.html")
class ActionFormDataLifecyclePage {
    private int initialCalls;
    private int actionCalls;

    @FormData("step")
    FormDataLifecycleForm initialStep() {
        initialCalls++;
        return new FormDataLifecycleForm("initial-step-" + initialCalls);
    }

    @Action
    @FormData("step")
    FormDataLifecycleForm selectStep() {
        actionCalls++;
        return new FormDataLifecycleForm("selected-step-" + actionCalls);
    }

    int getInitialCalls() {
        return initialCalls;
    }

    int getActionCalls() {
        return actionCalls;
    }
}
