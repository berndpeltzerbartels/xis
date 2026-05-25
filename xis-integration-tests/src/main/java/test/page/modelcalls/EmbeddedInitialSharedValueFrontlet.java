package test.page.modelcalls;

import one.xis.Frontlet;
import one.xis.ModelData;
import one.xis.ModelDataLoad;
import one.xis.SharedValue;

@Frontlet
class EmbeddedInitialSharedValueFrontlet {
    private int rootCalls;
    private int initialSelectionCalls;
    private int titleCalls;

    @SharedValue("root")
    String root() {
        rootCalls++;
        return "root";
    }

    @ModelData(varName = "selected", load = ModelDataLoad.INITIAL)
    @SharedValue("selected")
    String selected(@SharedValue("root") String root) {
        initialSelectionCalls++;
        return root + "-selected";
    }

    @ModelData
    String title(@SharedValue("selected") String selected) {
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
