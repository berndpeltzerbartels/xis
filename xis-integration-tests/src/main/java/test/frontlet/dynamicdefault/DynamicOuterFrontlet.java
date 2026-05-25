package test.frontlet.dynamicdefault;

import one.xis.Frontlet;
import one.xis.Action;
import one.xis.ModelData;

@Frontlet
class DynamicOuterFrontlet {
    private int reloads;
    private long selectedPipelineId = 42L;
    private long selectedStepId = 7L;

    @ModelData
    String innerFrontletId() {
        return "DynamicInnerFormFrontlet";
    }

    @ModelData
    long selectedPipelineId() {
        return selectedPipelineId;
    }

    @ModelData
    long selectedStepId() {
        return selectedStepId;
    }

    @ModelData
    int reloads() {
        return reloads;
    }

    @Action
    void reload() {
        reloads++;
    }

    @Action
    void selectOther() {
        selectedPipelineId = 43L;
        selectedStepId = 8L;
    }
}
