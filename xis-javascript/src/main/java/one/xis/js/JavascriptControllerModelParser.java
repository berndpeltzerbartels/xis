package one.xis.js;

import one.xis.OnAction;
import one.xis.context.XISComponent;
import one.xis.controller.ControllerUtils;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

@XISComponent
public class JavascriptControllerModelParser {

    public void parseControllerModel(Class<?> controllerClass, JSClass component) {
        overrideGetPhaseStateKeys(controllerClass, component);
        overrideGetAcrtionStateKeys(controllerClass, component);
    }

    private void overrideGetPhaseStateKeys(Class<?> controllerClass, JSClass component) {
        var getPhaseStateKeys = component.overrideAbstractMethod("getPhaseStateKeys");
        var keyMapVar = new JSVar("keyMap");
        var keyMap = new JSObject();
        keyMap.addField("init", initComponentStateKeyArray(controllerClass));
        keyMap.addField("destroy", destroyCompoentStateKeyArray(controllerClass));
        keyMap.addField("show", showCompoentStateKeyArray(controllerClass));
        keyMap.addField("hide", hideCompoentStateKeyArray(controllerClass));
        getPhaseStateKeys.addStatement(new JSVarAssignment(keyMapVar, keyMap));
        getPhaseStateKeys.addStatement(new JSCustomStatement(String.format("return %s[%s]", keyMapVar.getName(), getPhaseStateKeys.getArgs().get(0))));
    }

    private void overrideGetAcrtionStateKeys(Class<?> controllerClass, JSClass component) {
        var getActionStateKeys = component.overrideAbstractMethod("getPhaseStateKeys");
    }

    private JSArray initComponentStateKeyArray(Class<?> controllerClass) {
        return componentStateArray(ControllerUtils.getOnInitMethods(controllerClass));
    }

    private JSArray destroyCompoentStateKeyArray(Class<?> controllerClass) {
        return componentStateArray(ControllerUtils.getOnDestroyMethods(controllerClass));
    }

    private JSArray showCompoentStateKeyArray(Class<?> controllerClass) {
        return componentStateArray(ControllerUtils.getOnShowMethods(controllerClass));
    }

    private JSArray hideCompoentStateKeyArray(Class<?> controllerClass) {
        return componentStateArray(ControllerUtils.getOnHideMethods(controllerClass));
    }

    private JSArray componentStateArray(Stream<Method> methods) {
        return new JSArray(methods.flatMap(this::getComponentStateParameterKeys)
                .map(JSString::new)
                .toArray(JSString[]::new));
    }


    private void overrideGetComponentStateKeysOnShowMethod(JSClass owner, Class<?> controllerClass) {
        JSMethod getComponentStateKeysOnShow = owner.getMethod("getOnShowComponentStateKeys");
        getComponentStateKeysOnShow.addStatement(new JSReturn(getComponentStateKeysOnShow(controllerClass)));
    }

    private void overrideGetComponentStateKeysMethod(JSClass owner, Class<?> controllerClass, String methodName, Function<Class<?>, JSArray> keyProvider) {
        JSMethod getComponentStateKeysOnShow = owner.getMethod(methodName);
        getComponentStateKeysOnShow.addStatement(new JSReturn(keyProvider.apply(controllerClass)));
    }


    private JSArray getComponentStateKeysOnShow(Class<?> controllerClass) {
        return new JSArray(ControllerUtils.getOnShowMethods(controllerClass)
                .flatMap(this::getComponentStateParameterKeys)
                .map(JSString::new)
                .toArray(JSString[]::new));
    }

    private JSArray getComponentStateKeysOnHide(Class<?> controllerClass) {
        return new JSArray(ControllerUtils.getOnHideMethods(controllerClass)
                .flatMap(this::getComponentStateParameterKeys)
                .map(JSString::new)
                .toArray(JSString[]::new));
    }

    private Collector<String, JSArray, JSArray> collectToKeyArray() {
        return null; //Collector.of(JSArray::new, );
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
        return new JSArray(getComponentStateParameterKeys(method)
                .map(JSString::new)
                .toArray(JSString[]::new));
    }

    private Stream<String> getComponentStateParameterKeys(Method method) {
        return ControllerUtils.getComponentStateParamters(method)
                .map(ControllerUtils::getClientAttributeKey);
    }

    private JSArray getComponentStateParameterKeyArray(Method method) {
        return new JSArray(ControllerUtils.getModelParamters(method)
                .map(ControllerUtils::getModelKey)
                .map(JSString::new)
                .toArray(JSString[]::new));
    }


}
