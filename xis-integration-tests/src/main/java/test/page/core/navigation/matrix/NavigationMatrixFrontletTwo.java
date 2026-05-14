package test.page.core.navigation.matrix;

import one.xis.Frontlet;
import one.xis.ModelData;

@Frontlet(containerId = "main")
class NavigationMatrixFrontletTwo {

    @ModelData
    String message() {
        return "from-class";
    }
}
