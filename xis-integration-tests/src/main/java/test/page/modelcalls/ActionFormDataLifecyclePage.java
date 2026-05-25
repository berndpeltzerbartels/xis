package test.page.modelcalls;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Page;

@Page("/action-form-data-lifecycle.html")
class ActionFormDataLifecyclePage {
    private int initialCalls;
    private int actionCalls;
    private int namedActionCalls;

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

    @Action("namedAction")
    @FormData("namedStep")
    FormDataLifecycleForm namedStep() {
        namedActionCalls++;
        return new FormDataLifecycleForm("named-step-" + namedActionCalls);
    }

    int getInitialCalls() {
        return initialCalls;
    }

    int getActionCalls() {
        return actionCalls;
    }

    int getNamedActionCalls() {
        return namedActionCalls;
    }
}
