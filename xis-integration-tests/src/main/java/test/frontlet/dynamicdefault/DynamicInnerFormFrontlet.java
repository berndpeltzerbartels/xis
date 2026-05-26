package test.frontlet.dynamicdefault;

import one.xis.FormData;
import one.xis.Frontlet;
import one.xis.FrontletParameter;

@Frontlet
class DynamicInnerFormFrontlet {

    @FormData("step")
    StepForm step(@FrontletParameter("pipelineId") long pipelineId, @FrontletParameter("stepId") long stepId) {
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
