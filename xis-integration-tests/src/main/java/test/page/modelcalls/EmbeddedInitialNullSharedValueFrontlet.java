package test.page.modelcalls;

import one.xis.Frontlet;
import one.xis.ModelData;
import one.xis.ModelDataLoad;
import one.xis.NullAllowed;
import one.xis.SharedValue;

@Frontlet
class EmbeddedInitialNullSharedValueFrontlet {
    private int initialSelectionCalls;
    private int titleCalls;

    @ModelData(varName = "nullSelected", load = ModelDataLoad.INITIAL)
    @SharedValue("nullSelected")
    Object selected() {
        initialSelectionCalls++;
        return null;
    }

    @ModelData
    String nullTitle(@SharedValue("nullSelected") @NullAllowed Object selected) {
        titleCalls++;
        return selected == null ? "new" : "edit";
    }

    int getInitialSelectionCalls() {
        return initialSelectionCalls;
    }

    int getTitleCalls() {
        return titleCalls;
    }
}
