package one.xis.controller;

import lombok.RequiredArgsConstructor;
import one.xis.context.XISComponent;
import one.xis.reflect.MethodSignature;
import one.xis.utils.lang.MethodUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@XISComponent
@RequiredArgsConstructor
class ControllerMethodFactory {

    private final MethodParameterFactory methodParameterFactory;

    Stream<ControllerMethod> controllerMethods(Class<?> controllerClass) {
        return MethodUtils.methods(controllerClass).stream().map(this::toControllerMethod);
    }

    private ControllerMethod toControllerMethod(Method method) {
        return ControllerMethod.builder()
                .methodSignature(MethodSignature.from(method))
                .methodParameters(methodParameters(method))
                .methodAnnotations(methodAnnotations(method))
                .returnType(method.getReturnType())
                .name(method.getName())
                .parameterTypes(parameterTypes(method))
                .build();
    }

    private Class<?>[] parameterTypes(Method method) {
        return Arrays.stream(method.getParameters()).map(Parameter::getType).toArray(Class<?>[]::new);
    }

    private Collection<Annotation> methodAnnotations(Method method) {
        return Arrays.asList(method.getAnnotations());
    }

    private List<MethodParameter> methodParameters(Method method) {
        return methodParameterFactory.methodParameters(method);
    }
}
