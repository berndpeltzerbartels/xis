package one.xis.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import one.xis.OnDestroy;
import one.xis.OnHide;
import one.xis.OnInit;
import one.xis.OnShow;
import one.xis.ajax.AjaxResponseMessage;
import one.xis.ajax.InvocationContext;
import one.xis.ajax.Phase;
import one.xis.context.XISComponent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@XISComponent
@RequiredArgsConstructor
public class ControllerInvocationService {

    private final ControllerParameterProvider parameterProvider;

    public Collection<AjaxResponseMessage> invokeController(@NonNull Object controller, @NonNull InvocationContext invocationContext) {
        return getAnnotatedMethods(controller, getPhaseAnnotation(invocationContext.getPhase()))
                .map(method -> invokeControllerMethod(controller, method, invocationContext))
                .collect(Collectors.toSet());
    }

    private AjaxResponseMessage invokeControllerMethod(@NonNull Object controller, @NonNull Method method, @NonNull InvocationContext invocationContext) {
        return null; // TODO
    }


    private Class<? extends Annotation> getPhaseAnnotation(Phase phase) {
        switch (phase) {
            case SHOW:
                return OnShow.class;
            case HIDE:
                return OnHide.class;
            case INIT:
                return OnInit.class;
            case DESTROY:
                return OnDestroy.class;
            default:
                throw new IllegalArgumentException();
        }
    }


    private Object[] prepareArgs(@NonNull Method method, @NonNull InvocationContext invocationContext) {
        var rv = new Object[method.getParameters().length];
        for (int i = 0; i < rv.length; i++) {
            rv[i] = paramValue(method.getParameters()[i], invocationContext);
        }
        return rv;
    }

    private Object paramValue(@NonNull Parameter parameter, @NonNull InvocationContext invocationContext) {
        return parameterProvider.paramValue(parameter, invocationContext);
    }


    private Stream<Method> getAnnotatedMethods(Object controller, Class<? extends Annotation> annotationClass) {
        return ControllerUtils.getAnnotatedMethods(controller.getClass(), annotationClass);
    }

}
