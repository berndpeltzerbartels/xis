package test.page.modelcalls;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.ModelDataLoad;
import one.xis.Page;

@Page("/model-data-lifecycle.html")
class ModelDataLifecyclePage {
    private int alwaysCalls;
    private int initialCalls;
    private int afterActionCalls;

    @ModelData("always")
    String always() {
        return "always-" + ++alwaysCalls;
    }

    @ModelData(value = "initial", load = ModelDataLoad.INITIAL)
    String initial() {
        return "initial-" + ++initialCalls;
    }

    @ModelData(value = "afterAction", load = ModelDataLoad.AFTER_ACTION)
    String afterAction() {
        return "after-action-" + ++afterActionCalls;
    }

    @ModelData(varName = "selected", load = ModelDataLoad.INITIAL)
    String initiallySelected() {
        return "first";
    }

    @Action
    @ModelData("selected")
    String selectSecond() {
        return "second";
    }

    int getAlwaysCalls() {
        return alwaysCalls;
    }

    int getInitialCalls() {
        return initialCalls;
    }

    int getAfterActionCalls() {
        return afterActionCalls;
    }
}
