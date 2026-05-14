package test.page.core.navigation.matrix;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.PathVariable;
import one.xis.QueryParameter;

@Page("/navigation/detail/{id}.html")
class NavigationMatrixDetailPage {

    @ModelData
    String detailId(@PathVariable("id") String id) {
        return id;
    }

    @ModelData
    String detailMode(@QueryParameter("mode") String mode) {
        return mode;
    }
}
