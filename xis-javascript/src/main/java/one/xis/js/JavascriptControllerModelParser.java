package one.xis.js;

import one.xis.OnAction;
import one.xis.context.XISComponent;
import one.xis.controller.ControllerUtils;

import java.lang.reflect.Method;
import java.util.stream.Stream;

@XISComponent
public class JavascriptControllerModelParser {

    public void parseControllerModel(Class<?> controllerClass, JSClass component) {
        overrideGetActiveActions(controllerClass, component);
        overrideGetActivePhases(controllerClass, component);
        overrideGetActionStateKeys(controllerClass, component);
        overrideGetOnInitStateKeys(controllerClass, component);
        overrideGetOnDestroyStateKeys(controllerClass, component);
        overrideGetOnShowStateKeys(controllerClass, component);
        overrideGetOnHideStateKeys(controllerClass, component);
    }

    private void overrideGetActiveActions(Class<?> controllerClass, JSClass component) {
        var getActiveActions = component.overrideAbstractMethod("getActiveActions");
        getActiveActions.addStatement(new JSReturn(activeActionsArray(controllerClass)));
    }

    private void overrideGetActivePhases(Class<?> controllerClass, JSClass component) {
        var getActiveActions = component.overrideAbstractMethod("getActivePhases");
        getActiveActions.addStatement(new JSReturn(activePhasesArray(controllerClass)));
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


    private JSArray activeActionsArray(Class<?> controllerClass) {
        return new JSArray(ControllerUtils.getActionMethods(controllerClass).stream()
                .map(m -> m.getAnnotation(OnAction.class))
                .map(OnAction::value)
                .map(JSString::new)
                .toArray(JSString[]::new));
    }

    private JSArray activePhasesArray(Class<?> controllerClass) {
        var array = new JSArray();
        if (ControllerUtils.getOnInitMethods(controllerClass).count() > 0) {
            array.getElements().add(new JSString("init"));
        }
        if (ControllerUtils.getOnDestroyMethods(controllerClass).count() > 0) {
            array.getElements().add(new JSString("destroy"));
        }
        if (ControllerUtils.getOnShowMethods(controllerClass).count() > 0) {
            array.getElements().add(new JSString("show"));
        }
        if (ControllerUtils.getOnHideMethods(controllerClass).count() > 0) {
            array.getElements().add(new JSString("hide"));
        }
        return array;
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
