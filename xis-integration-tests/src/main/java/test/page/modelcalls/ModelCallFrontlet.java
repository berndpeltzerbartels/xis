package test.page.modelcalls;

import lombok.RequiredArgsConstructor;
import one.xis.Frontlet;
import one.xis.ModelData;

@Frontlet
@RequiredArgsConstructor
class ModelCallFrontlet {
    private final ModelCallCounter counter;

    @ModelData("frontletCalls")
    int frontletCalls() {
        return counter.frontletModelCalls();
    }
}
