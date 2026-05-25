package test.frontlet.dynamicdefault;

import one.xis.FormData;
import one.xis.Frontlet;
import one.xis.Parameter;

@Frontlet
class DynamicInnerFormFrontlet {

    @FormData("step")
    StepForm step(@Parameter("pipelineId") long pipelineId, @Parameter("stepId") long stepId) {
        var form = new StepForm();
        form.pipelineId = pipelineId;
        form.stepId = stepId;
        return form;
    }

    public static class StepForm {
        public long pipelineId;
        public long stepId;
    }
}
