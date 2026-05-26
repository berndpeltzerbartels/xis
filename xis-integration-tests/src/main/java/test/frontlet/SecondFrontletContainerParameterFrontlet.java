package test.frontlet;

import one.xis.Frontlet;
import one.xis.ModelData;
import one.xis.FrontletParameter;

@Frontlet
class SecondFrontletContainerParameterFrontlet {

    @ModelData("categoryId")
    String categoryId(@FrontletParameter("categoryId") String categoryId) {
        return categoryId;
    }
}
