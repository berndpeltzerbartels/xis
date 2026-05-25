package test.page.modelcalls;

import one.xis.Frontlet;
import one.xis.ModelData;
import one.xis.ModelDataLoad;
import one.xis.SharedValue;

@Frontlet
class EmbeddedInitialSharedValueNestedFrontlet {
    private int rootCalls;
    private int initialSelectionCalls;
    private int titleCalls;

    @SharedValue("nestedRoot")
    String root() {
        rootCalls++;
        return "nested-root";
    }

    @ModelData(varName = "nestedSelected", load = ModelDataLoad.INITIAL)
    @SharedValue("nestedSelected")
    String selected(@SharedValue("nestedRoot") String root) {
        initialSelectionCalls++;
        return root + "-selected";
    }

    @ModelData
    String nestedTitle(@SharedValue("nestedSelected") String selected) {
        titleCalls++;
        return "title-" + selected;
    }

    int getRootCalls() {
        return rootCalls;
    }

    int getInitialSelectionCalls() {
        return initialSelectionCalls;
    }

    int getTitleCalls() {
        return titleCalls;
    }
}
