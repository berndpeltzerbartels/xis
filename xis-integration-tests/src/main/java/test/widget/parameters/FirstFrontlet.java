package test.widget.parameters;

import one.xis.Action;
import one.xis.Frontlet;
import one.xis.FrontletResponse;

@Frontlet
public class FirstFrontlet {

    @Action("switchWidget")
    public FrontletResponse switchWidget() {
        return new FrontletResponse(SecondFrontlet.class)
                .frontletParameter("actionParam", "actionValue");
    }
}
