package test.frontlet.dynamicdefault;

import one.xis.Frontlet;
import one.xis.ModelData;

@Frontlet
class DynamicOuterFrontlet {

    @ModelData
    String innerFrontlet() {
        return "DynamicInnerFormFrontlet?pipelineId=42&stepId=7";
    }
}
