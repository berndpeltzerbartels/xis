package one.xis.controller;


import lombok.Builder;
import lombok.Getter;
import one.xis.dto.RequestContext;
import one.xis.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

@Builder
@Getter
public class ControllerMethod {
    private final MethodSignature methodSignature;
    private final List<MethodParameter> methodParameters;
    private final Collection<Annotation> methodAnnotations;
    private final Class<?> returnType;
    private final String name;
    private final Class<?>[] parameterTypes;

    public Set<String> getParameterModelIds() {
        return methodParameters.stream()
                .filter(ModelParameter.class::isInstance)
                .map(ModelParameter.class::cast)
                .map(ModelParameter::getId)
                .collect(Collectors.toSet());
    }

    public <A extends Annotation> Optional<A> annotation(Class<A> annotationClass) {
        return methodAnnotations.stream().filter(a -> a.annotationType().equals(annotationClass)).map(annotationClass::cast).findFirst();
    }

    boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return methodAnnotations.stream().map(Annotation::annotationType).anyMatch(type -> type.equals(annotationClass));
    }

    Method getMethod(Object owner) {
        try {
            return owner.getClass().getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    Object[] getArgs(RequestContext context) {
        var args = new ArrayList<>();
        for (MethodParameter parameter : getMethodParameters()) {
            if (parameter instanceof ModelParameter) {
                args.add(context.getModel());
            } else if (parameter instanceof UserIdParameter) {
                args.add(getUserId(context.getToken()));
            } else if (parameter instanceof TokenParamter) {
                args.add(context.getToken());
            } else if (parameter instanceof StateParameter) {
                args.add(context.getState(((StateParameter) parameter).getName()));
            } else {
                throw new IllegalStateException();
            }
        }
        return args.toArray();
    }

    private Object getUserId(String token) {
        return null;
    }

}
