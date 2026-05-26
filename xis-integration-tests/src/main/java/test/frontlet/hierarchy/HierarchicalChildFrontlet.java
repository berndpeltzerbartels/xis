package test.frontlet.hierarchy;

import one.xis.Frontlet;
import one.xis.FrontletParameter;
import one.xis.ModelData;

@Frontlet
class HierarchicalChildFrontlet {

    @ModelData("projectId")
    String projectId(@FrontletParameter("projectId") String projectId) {
        return projectId;
    }

    @ModelData("stepId")
    String stepId(@FrontletParameter("stepId") String stepId) {
        return stepId;
    }

    @ModelData("view")
    String view(@FrontletParameter("view") String view) {
        return view;
    }
}
