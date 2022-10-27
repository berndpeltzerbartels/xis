package one.xis.controller;


import lombok.Builder;
import one.xis.reflect.MethodSignature;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Builder
public class ControllerMethod {
    private final MethodSignature methodSignature;
    private final List<MethodParameter> methodParameters;
    private final Collection<Annotation> methodAnnotations;

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

}
