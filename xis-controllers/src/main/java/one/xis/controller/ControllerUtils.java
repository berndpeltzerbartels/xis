package one.xis.controller;

import lombok.NonNull;
import one.xis.*;
import one.xis.utils.lang.MethodUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ControllerUtils {

    public static Stream<Method> getOnInitMethods(Class<?> controllerClass) {
        return getAnnotatedMethods(controllerClass, OnInit.class);
    }

    public static Stream<Method> getOnDestroyMethods(Class<?> controllerClass) {
        return getAnnotatedMethods(controllerClass, OnDestroy.class);
    }

    public static Stream<Method> getOnShowMethods(Class<?> controllerClass) {
        return getAnnotatedMethods(controllerClass, OnShow.class);
    }

    public static Stream<Method> getOnHideMethods(Class<?> controllerClass) {
        return getAnnotatedMethods(controllerClass, OnHide.class);
    }


    public static Set<Method> getActionMethods(Class<?> controllerClass) {
        return getAnnotatedMethods(controllerClass, Action.class).collect(Collectors.toSet());
    }

    public static Stream<Parameter> getModelParamters(Method method) {
        return Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.isAnnotationPresent(Model.class));
    }

    public static Stream<Parameter> getComponentStateParamters(Method method) {
        return Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.isAnnotationPresent(ComponentState.class));
    }

    public static <A extends Annotation> Stream<Method> getAnnotatedMethods(Class<?> controllerClass, Class<A> annotationClass) {
        return MethodUtils.methods(controllerClass).stream()
                .filter(method -> method.isAnnotationPresent(annotationClass));
    }

    public static String getClientAttributeKey(Parameter parameter) {
        var annotation = parameter.getAnnotation(ClientAttribute.class);
        return annotation.value().isEmpty() ? parameter.getName() : annotation.value();
    }

    public static String getModelKey(Parameter parameter) {
        var annotation = parameter.getAnnotation(Model.class);
        return annotation.value().isEmpty() ? parameter.getName() : annotation.value();
    }

    public static String getClientAttributeKey(java.lang.reflect.Method method) {
        var annotation = method.getAnnotation(ClientAttribute.class);
        return annotation.value().isEmpty() ? method.getName() : annotation.value();
    }

    public static String getModelKey(Method method) {
        var annotation = method.getAnnotation(Model.class);
        return annotation.value().isEmpty() ? method.getReturnType().getSimpleName() : annotation.value();
    }

    public static boolean isPageControllerClass(@NonNull Class<?> controllerClass) {
        return controllerClass.isAnnotationPresent(Page.class);
    }


    public static boolean isWidgetControllerClass(@NonNull Class<?> controllerClass) {
        return controllerClass.isAnnotationPresent(Widget.class);
    }

    @NonNull
    public static String getControllerId(Class<?> controllerClass) {
        if (controllerClass.isAnnotationPresent(Widget.class)) {
            return getWidgetControllerId(controllerClass);
        }
        if (controllerClass.isAnnotationPresent(Page.class)) {
            return getPageControllerPath(controllerClass);
        }
        throw new IllegalStateException();
    }

    public static String getWidgetControllerId(Class<?> controllerClass) {
        var annotation = controllerClass.getAnnotation(Widget.class);
        return annotation.value().isEmpty() ? controllerClass.getSimpleName() : annotation.value();
    }

    public static String getPageControllerPath(Class<?> controllerClass) {
        return controllerClass.getAnnotation(Page.class).path();
    }
}
