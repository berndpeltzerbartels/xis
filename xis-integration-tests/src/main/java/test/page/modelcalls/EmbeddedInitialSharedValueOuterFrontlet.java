package test.page.modelcalls;

import one.xis.Frontlet;
import one.xis.ModelData;

@Frontlet
class EmbeddedInitialSharedValueOuterFrontlet {

    @ModelData
    String innerFrontlet() {
        return "EmbeddedInitialSharedValueNestedFrontlet";
    }
}
