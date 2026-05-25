package test.frontlet.dynamicdefault;

import one.xis.Frontlet;
import one.xis.Action;
import one.xis.ModelData;

@Frontlet
class DynamicOuterFrontlet {
    private int reloads;

    @ModelData
    String innerFrontletId() {
        return "DynamicInnerFormFrontlet";
    }

    @ModelData
    long selectedPipelineId() {
        return 42L;
    }

    @ModelData
    long selectedStepId() {
        return 7L;
    }

    @ModelData
    int reloads() {
        return reloads;
    }

    @Action
    void reload() {
        reloads++;
    }
}
