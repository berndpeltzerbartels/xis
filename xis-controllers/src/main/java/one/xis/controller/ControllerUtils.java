package one.xis.controller;

import one.xis.Model;
import one.xis.OnAction;
import one.xis.State;
import one.xis.utils.lang.MethodUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ControllerUtils {

    public static Stream<Method> getInitializerMethods(Class<?> controllerClass) {
        return getAnnotatedMethods(controllerClass, Model.class);
    }

    public static Set<Method> getActionMethods(Class<?> controllerClass) {
        return getAnnotatedMethods(controllerClass, OnAction.class).collect(Collectors.toSet());
    }

    public static Stream<Parameter> getModelParamters(Method method) {
        return Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.isAnnotationPresent(Model.class));
    }

    public static Stream<Parameter> getStateParamters(Method method) {
        return Arrays.stream(method.getParameters())
                .filter(parameter -> parameter.isAnnotationPresent(State.class));
    }

    public static <A extends Annotation> Stream<Method> getAnnotatedMethods(Class<?> controllerClass, Class<A> annotationClass) {
        return MethodUtils.methods(controllerClass).stream()
                .filter(method -> method.isAnnotationPresent(annotationClass));
    }

    public static String getStateKey(Parameter parameter) {
        var annotation = parameter.getAnnotation(State.class);
        return annotation.value().isEmpty() ? parameter.getName() : annotation.value();
    }

    public static String getModelKey(Parameter parameter) {
        var annotation = parameter.getAnnotation(Model.class);
        return annotation.value().isEmpty() ? parameter.getName() : annotation.value();
    }

    public static String getStateKey(java.lang.reflect.Method method) {
        var annotation = method.getAnnotation(State.class);
        return annotation.value().isEmpty() ? method.getName() : annotation.value();
    }

    public static String getModelKey(Method method) {
        var annotation = method.getAnnotation(Model.class);
        return annotation.value().isEmpty() ? method.getName() : annotation.value();
    }

}
