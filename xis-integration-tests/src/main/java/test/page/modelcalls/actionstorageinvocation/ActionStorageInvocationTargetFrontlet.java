package test.page.modelcalls.actionstorageinvocation;

import lombok.RequiredArgsConstructor;
import one.xis.ClientState;
import one.xis.Frontlet;
import one.xis.ModelData;
import one.xis.SharedValue;

@Frontlet
@RequiredArgsConstructor
class ActionStorageInvocationTargetFrontlet {
    private final ActionStorageInvocationCounter counter;

    @SharedValue("targetActionStorageToken")
    String token() {
        counter.add("target.shared");
        return "target-token";
    }

    @ModelData("targetModel")
    String model(@SharedValue("targetActionStorageToken") String token) {
        counter.add("target.model:" + token);
        return "target-model";
    }

    @ClientState("targetState")
    String state(@SharedValue("targetActionStorageToken") String token) {
        counter.add("target.state:" + token);
        return "target-state";
    }

    @ModelData("targetModelAndState")
    @ClientState("targetModelAndState")
    String modelAndState(@SharedValue("targetActionStorageToken") String token) {
        counter.add("target.modelAndState:" + token);
        return "target-model-and-state";
    }
}
