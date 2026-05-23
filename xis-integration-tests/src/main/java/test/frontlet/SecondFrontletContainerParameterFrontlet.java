package test.frontlet;

import one.xis.Frontlet;
import one.xis.ModelData;
import one.xis.Parameter;

@Frontlet
class SecondFrontletContainerParameterFrontlet {

    @ModelData("categoryId")
    String categoryId(@Parameter("categoryId") String categoryId) {
        return categoryId;
    }
}
