package test.widget.parameters;

import one.xis.ModelData;
import one.xis.Frontlet;
import one.xis.FrontletParameter;

@Frontlet
public class SecondFrontlet {

    @ModelData
    public String combinedParams(
            @FrontletParameter("actionParam") String actionParam,
            @FrontletParameter("containerParam") String containerParam) {
        return "Action: " + actionParam + ", Container: " + containerParam;
    }

    @ModelData
    public String actionParam(@FrontletParameter("actionParam") String actionParam) {
        return actionParam;
    }

    @ModelData
    public String containerParam(@FrontletParameter("containerParam") String containerParam) {
        return containerParam;
    }
}
