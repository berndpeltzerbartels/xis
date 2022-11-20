package one.xis.js;

import one.xis.OnAction;
import one.xis.context.XISComponent;
import one.xis.controller.ControllerUtils;

import java.lang.reflect.Method;
import java.util.stream.Stream;

@XISComponent
public class JavascriptControllerModelParser {

    public void parseControllerModel(Class<?> controllerClass, JSClass component) {
        var keyObjCompState = new JSObject();
        keyObjCompState.addField("init", initialClientStateKeyArray(controllerClass));
        keyObjCompState.addField("show", onShowClientStateKeyArray(controllerClass));
        keyObjCompState.addField("action", actionComponentStateKeys(controllerClass));
        component.addField("compKeys", keyObjCompState);

        var keyObjClientState = new JSObject();
        keyObjClientState.addField("init", initialClientStateKeyArray(controllerClass));
        keyObjClientState.addField("show", onShowClientStateKeyArray(controllerClass));
        keyObjClientState.addField("action", actionComponentStateKeys(controllerClass));
        component.addField("clientStateKeys", keyObjClientState);

    }

    private JSArray initialClientStateKeyArray(Class<?> controllerClass) {
        return new JSArray(ControllerUtils.getInitializerMethods(controllerClass)
                .flatMap(this::getClientStateParameterKeys)
                .map(JSString::new)
                .toArray(JSString[]::new));
    }

    private JSArray onShowClientStateKeyArray(Class<?> controllerClass) {
        return new JSArray(ControllerUtils.getOnShowMethods(controllerClass)
                .flatMap(this::getClientStateParameterKeys)
                .map(JSString::new)
                .toArray(JSString[]::new));
    }


    private JSObject actionClientStateKeyObj(Class<?> controllerClass) {
        var object = new JSObject();
        ControllerUtils.getActionMethods(controllerClass)
                .forEach(method -> {
                    var action = method.getAnnotation(OnAction.class).value();
                    object.addField(action, getClientStateParameterKeyArray(method));
                });

        return object;
    }

    private JSObject actionComponentStateKeys(Class<?> controllerClass) {
        var object = new JSObject();
        ControllerUtils.getActionMethods(controllerClass)
                .forEach(method -> {
                    var action = method.getAnnotation(OnAction.class).value();
                    object.addField(action, getComponentStateParameterKeyArray(method));
                });

        return object;
    }

    private JSArray getClientStateParameterKeyArray(Method method) {
        return new JSArray(getClientStateParameterKeys(method)
                .map(JSString::new)
                .toArray(JSString[]::new));
    }

    private Stream<String> getClientStateParameterKeys(Method method) {
        return ControllerUtils.getStateParamters(method)
                .map(ControllerUtils::getStateKey);
    }

    private JSArray getComponentStateParameterKeyArray(Method method) {
        return new JSArray(ControllerUtils.getModelParamters(method)
                .map(ControllerUtils::getModelKey)
                .map(JSString::new)
                .toArray(JSString[]::new));
    }


}
