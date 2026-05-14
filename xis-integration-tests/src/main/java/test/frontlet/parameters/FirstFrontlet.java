package test.frontlet.parameters;

import one.xis.Action;
import one.xis.Frontlet;
import one.xis.FrontletResponse;

@Frontlet
public class FirstFrontlet {

    @Action("switchFrontlet")
    public FrontletResponse switchFrontlet() {
        return new FrontletResponse(SecondFrontlet.class)
                .frontletParameter("actionParam", "actionValue");
    }
}
