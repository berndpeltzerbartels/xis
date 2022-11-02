package one.xis.js;

import one.xis.context.XISComponent;

import java.util.List;

@XISComponent
public class JavascriptControllerModelParser {

    public void parseControllerModel(Class<?> controllerClass, JSClass component) {
        overrideSendInit(controllerClass, component);
    }

    private void overrideSendInit(Class<?> controllerClass, JSClass component) {
        // TODO
        var sendInit = component.overrideAbstractMethod("loadModel");
        var initMethodSignatures = new JSVar("signatures");
        //sendInit.addStatement(new JSVarAssignment(initMethodSignatures, new JSArray(getInitMethodSignatures(controllerClass))));
        var callRemoteInitArgs = new JSVar[]{initMethodSignatures, new JSVar("this")};
        var callRemoteInitMethod = new JSMethod(new JSClass("XISClient"), "callRemoteInit", List.of("signatures", "component"));
        var callRemoteInitMethodCall = new JSMethodCall(callRemoteInitMethod, callRemoteInitArgs);
        sendInit.addStatement(callRemoteInitMethodCall);
    }


}
