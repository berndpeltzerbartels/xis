package one.xis.js;

import one.xis.InitModel;
import one.xis.context.XISComponent;
import one.xis.controller.ControllerMethod;
import one.xis.controller.ControllerModel;
import one.xis.reflect.MethodSignature;

import java.util.List;

@XISComponent
public class JavascriptControllerModelParser {

    public void parseControllerModel(ControllerModel controllerModel, JSClass component) {
        overrideSendInit(controllerModel, component);
    }

    private void overrideSendInit(ControllerModel controllerModel, JSClass component) {
        var sendInit = component.overrideAbstractMethod("loadModel");
        var initMethodSignatures = new JSVar("signatures");
        sendInit.addStatement(new JSVarAssignment(initMethodSignatures, new JSArray(getInitMethodSignatures(controllerModel))));
        var callRemoteInitArgs = new JSVar[]{initMethodSignatures, new JSVar("this")};
        var callRemoteInitMethod = new JSMethod(new JSClass("XISClient"), "callRemoteInit", List.of("signatures", "component"));
        var callRemoteInitMethodCall = new JSMethodCall(callRemoteInitMethod, callRemoteInitArgs);
        sendInit.addStatement(callRemoteInitMethodCall);
    }


    private JSString[] getInitMethodSignatures(ControllerModel controllerModel) {
        return controllerModel.getAnnotatedMethods(InitModel.class)
                .map(ControllerMethod::getMethodSignature)
                .map(MethodSignature::toString)
                .map(JSString::new)
                .toArray(JSString[]::new);
    }

}
