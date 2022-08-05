package one.xis.invocation;

import one.xis.ClientId;
import one.xis.Model;
import one.xis.Token;
import one.xis.context.XISComponent;

import java.lang.annotation.Annotation;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@XISComponent
class MethodParameterFactory {

    Set<Class<? extends Annotation>> PARAMETER_ANNOTATIONS = Set.of(ClientId.class, Token.class);
    
    MethodParameter create(Parameter parameter) {
        return getParamAnnotation(parameter).map(annotation -> createByAnnotation(parameter, annotation)).orElseGet(() -> createModelParameter(parameter));
    }


    private Optional<Annotation> getParamAnnotation(Parameter parameter) {
        List<Annotation> annotationList = Arrays.stream(parameter.getAnnotations()).filter(this::isParamAnnotation).collect(Collectors.toList());
        switch (annotationList.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(annotationList.get(0));
            default:
                throw new IllegalStateException(parameter + " has contradictory annotations");
        }
    }

    private boolean isParamAnnotation(Annotation annotation) {
        return PARAMETER_ANNOTATIONS.contains(annotation.annotationType());
    }

    private MethodParameter createByAnnotation(Parameter parameter, Annotation annotation) {
        if (annotation.annotationType() == ClientId.class) {
            return new ClientIdParameter(parameter);
        }
        if (annotation.annotationType() == Token.class) {
            return new TokenParamter(parameter);
        }
        throw new IllegalStateException();
    }

    private MethodParameter createModelParameter(Parameter parameter) {
        if (!parameter.getType().isAnnotationPresent(Model.class)) {
            throw new IllegalStateException(parameter + ": looks like it should be a model-parameter, but the paramter-type is not annotated wit @Model");
        }
        return null;
    }

}
