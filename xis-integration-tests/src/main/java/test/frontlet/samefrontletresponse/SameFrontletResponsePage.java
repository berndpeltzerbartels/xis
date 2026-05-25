package test.frontlet.samefrontletresponse;

import one.xis.Page;
import one.xis.ModelData;

@Page("/same-frontlet-response.html")
class SameFrontletResponsePage {

    @ModelData
    String queryDefaultFrontlet() {
        return "SameFrontlet?pipelineId=1&stepId=1";
    }

    @ModelData
    String tagDefaultFrontlet() {
        return "SameFrontlet?pipelineId=10&stepId=10";
    }
}
