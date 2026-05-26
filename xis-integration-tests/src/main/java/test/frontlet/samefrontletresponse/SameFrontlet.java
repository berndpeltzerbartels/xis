package test.frontlet.samefrontletresponse;

import one.xis.Action;
import one.xis.Frontlet;
import one.xis.FrontletResponse;
import one.xis.ModelData;
import one.xis.FrontletParameter;

@Frontlet
class SameFrontlet {
    static String lastStep;

    @ModelData
    String pipeline(@FrontletParameter("pipelineId") long pipelineId) {
        return String.valueOf(pipelineId);
    }

    @ModelData
    String step(@FrontletParameter("stepId") long stepId) {
        lastStep = String.valueOf(stepId);
        return String.valueOf(stepId);
    }

    @Action
    FrontletResponse selectStep() {
        return new FrontletResponse("SameFrontlet?pipelineId=1&stepId=2");
    }

    @Action
    FrontletResponse selectTaggedStep() {
        return new FrontletResponse("SameFrontlet?pipelineId=1&stepId=20");
    }
}
