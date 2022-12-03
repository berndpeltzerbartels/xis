package one.xis.js;

import one.xis.OnAction;
import one.xis.context.XISComponent;
import one.xis.controller.ControllerUtils;

import java.lang.reflect.Method;
import java.util.stream.Stream;

@XISComponent
public class JavascriptControllerModelParser {

    public void parseControllerModel(Class<?> controllerClass, JSClass component) {
        overrideGetActionStateKeys(controllerClass, component);
        overrideGetOnInitStateKeys(controllerClass, component);
        overrideGetOnDestroyStateKeys(controllerClass, component);
        overrideGetOnShowStateKeys(controllerClass, component);
        overrideGetOnHideStateKeys(controllerClass, component);
    }

    private void overrideGetActionStateKeys(Class<?> controllerClass, JSClass component) {
        var getActionStateKeys = component.overrideAbstractMethod("getActionStateKeys");
        getActionStateKeys.addStatement(new JSReturn(actionComponentStateKeys(controllerClass)));
    }

    private void overrideGetOnInitStateKeys(Class<?> controllerClass, JSClass component) {
        var getOnInitStateKeys = component.overrideAbstractMethod("getOnInitStateKeys");
        getOnInitStateKeys.addStatement(new JSReturn(onInitComponentStateKeyArray(controllerClass)));
    }

    private void overrideGetOnDestroyStateKeys(Class<?> controllerClass, JSClass component) {
        var getOnDestroyStateKeys = component.overrideAbstractMethod("getOnDestroyStateKeys");
        getOnDestroyStateKeys.addStatement(new JSReturn(onDestroyComponentStateKeyArray(controllerClass)));
    }

    private void overrideGetOnShowStateKeys(Class<?> controllerClass, JSClass component) {
        var getOnShowStateKeys = component.overrideAbstractMethod("getOnShowStateKeys");
        getOnShowStateKeys.addStatement(new JSReturn(onShowComponentStateKeyArray(controllerClass)));
    }

    private void overrideGetOnHideStateKeys(Class<?> controllerClass, JSClass component) {
        var getOnHideStateKeys = component.overrideAbstractMethod("getOnHideStateKeys");
        getOnHideStateKeys.addStatement(new JSReturn(onHideComponentStateKeyArray(controllerClass)));
    }

    private JSArray onInitComponentStateKeyArray(Class<?> controllerClass) {
        return componentStateArray(ControllerUtils.getOnInitMethods(controllerClass));
    }

    private JSArray onDestroyComponentStateKeyArray(Class<?> controllerClass) {
        return componentStateArray(ControllerUtils.getOnDestroyMethods(controllerClass));
    }

    private JSArray onShowComponentStateKeyArray(Class<?> controllerClass) {
        return componentStateArray(ControllerUtils.getOnShowMethods(controllerClass));
    }

    private JSArray onHideComponentStateKeyArray(Class<?> controllerClass) {
        return componentStateArray(ControllerUtils.getOnHideMethods(controllerClass));
    }

    private JSArray componentStateArray(Stream<Method> methods) {
        return new JSArray(methods.flatMap(this::getComponentStateParameterKeys)
                .map(JSString::new)
                .toArray(JSString[]::new));
    }

    private JSObject actionComponentStateKeys(Class<?> controllerClass) {
        var object = new JSObject();
        ControllerUtils.getActionMethods(controllerClass)
                .forEach(method -> {
                    var action = method.getAnnotation(OnAction.class).value();
                    object.addField(action, componentStateArray(Stream.of(method)));
                });

        return object;
    }

    private Stream<String> getComponentStateParameterKeys(Method method) {
        return ControllerUtils.getComponentStateParamters(method)
                .map(ControllerUtils::getClientAttributeKey);
    }
}
