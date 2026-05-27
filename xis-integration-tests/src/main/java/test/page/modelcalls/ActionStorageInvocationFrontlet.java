package test.page.modelcalls;

import lombok.RequiredArgsConstructor;
import one.xis.Action;
import one.xis.ClientState;
import one.xis.Frontlet;
import one.xis.FrontletResponse;
import one.xis.ModelData;
import one.xis.SharedValue;

@Frontlet
@RequiredArgsConstructor
class ActionStorageInvocationFrontlet {
    private final ActionStorageInvocationCounter counter;

    @SharedValue("actionStorageToken")
    String token() {
        counter.add("source.shared");
        return "source-token";
    }

    @ModelData("sourceModel")
    String model(@SharedValue("actionStorageToken") String token) {
        counter.add("source.model:" + token);
        return "model";
    }

    @ClientState("sourceState")
    String state(@SharedValue("actionStorageToken") String token) {
        counter.add("source.state:" + token);
        return "state";
    }

    @ModelData("sourceModelAndState")
    @ClientState("sourceModelAndState")
    String modelAndState(@SharedValue("actionStorageToken") String token) {
        counter.add("source.modelAndState:" + token);
        return "model-and-state";
    }

    @Action("voidAction")
    void voidAction(@SharedValue("actionStorageToken") String token) {
        counter.add("source.voidAction:" + token);
    }

    @Action("sameFrontlet")
    FrontletResponse sameFrontlet(@SharedValue("actionStorageToken") String token) {
        counter.add("source.sameFrontlet:" + token);
        return new FrontletResponse(ActionStorageInvocationFrontlet.class).targetContainer("frontlet");
    }

    @Action("otherFrontlet")
    FrontletResponse otherFrontlet(@SharedValue("actionStorageToken") String token) {
        counter.add("source.otherFrontlet:" + token);
        return new FrontletResponse(ActionStorageInvocationTargetFrontlet.class).targetContainer("frontlet");
    }
}
