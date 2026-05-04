package test.page.core.navigation.matrix;

import one.xis.Frontlet;
import one.xis.FrontletParameter;
import one.xis.ModelData;

@Frontlet(containerId = "main")
class NavigationMatrixParameterizedFrontlet {

    @ModelData
    String message(@FrontletParameter("message") String message) {
        return message;
    }
}
