package test.frontlet.parameters;

import one.xis.ModelData;
import one.xis.Frontlet;
import one.xis.Parameter;

@Frontlet
public class SecondFrontlet {

    @ModelData
    public String combinedParams(
            @Parameter("actionParam") String actionParam,
            @Parameter("containerParam") String containerParam) {
        return "Action: " + actionParam + ", Container: " + containerParam;
    }

    @ModelData
    public String actionParam(@Parameter("actionParam") String actionParam) {
        return actionParam;
    }

    @ModelData
    public String containerParam(@Parameter("containerParam") String containerParam) {
        return containerParam;
    }
}
