package test.page.router;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.PathVariable;
import one.xis.QueryParameter;

@Page("/router-target/{id}.html")
class RouterTargetPage {

    @ModelData
    String targetId(@PathVariable("id") String id) {
        return id;
    }

    @ModelData
    String targetTab(@QueryParameter("tab") String tab) {
        return tab;
    }
}
