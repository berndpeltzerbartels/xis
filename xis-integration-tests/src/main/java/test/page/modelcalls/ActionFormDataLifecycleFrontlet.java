package test.page.modelcalls;

import one.xis.Action;
import one.xis.FormData;
import one.xis.Frontlet;

@Frontlet
class ActionFormDataLifecycleFrontlet {
    private int initialCalls;
    private int actionCalls;

    @FormData("step")
    FormDataLifecycleForm initialStep() {
        initialCalls++;
        return new FormDataLifecycleForm("frontlet-initial-step-" + initialCalls);
    }

    @Action
    @FormData("step")
    FormDataLifecycleForm selectStep() {
        actionCalls++;
        return new FormDataLifecycleForm("frontlet-selected-step-" + actionCalls);
    }

    int getInitialCalls() {
        return initialCalls;
    }

    int getActionCalls() {
        return actionCalls;
    }
}
