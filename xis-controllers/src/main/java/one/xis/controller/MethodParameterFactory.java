package one.xis.controller;

import one.xis.ClientId;
import one.xis.Model;
import one.xis.Token;
import one.xis.UserId;
import one.xis.context.XISComponent;
import one.xis.utils.reflect.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@XISComponent
class MethodParameterFactory {

    Set<Class<? extends Annotation>> PARAMETER_ANNOTATIONS = Set.of(ClientId.class, UserId.class, Token.class);

    List<MethodParameter> methodParameters(Method method) {
        return Arrays.stream(method.getParameters()).map(this::methodParameter).collect(Collectors.toList());
    }

    MethodParameter methodParameter(Parameter parameter) {
        return getParamAnnotation(parameter).map(this::createByAnnotation).orElseGet(() -> createModelParameter(parameter));
    }


    private Optional<Annotation> getParamAnnotation(Parameter parameter) {
        List<Annotation> annotationList = Arrays.stream(parameter.getAnnotations()).filter(this::isParamAnnotation).collect(Collectors.toList());
        switch (annotationList.size()) {
            case 0:
                return Optional.empty();
            case 1:
                return Optional.of(annotationList.get(0));
            default:
                throw new IllegalStateException(parameter + " has contradictory annotations:" + annotationListAsString(annotationList));
        }
    }

    private String annotationListAsString(List<Annotation> annotations) {
        return annotations.stream().map(Annotation::annotationType).map(Class::getSimpleName).collect(Collectors.joining(", "));
    }

    private boolean isParamAnnotation(Annotation annotation) {
        return PARAMETER_ANNOTATIONS.contains(annotation.annotationType());
    }

    private MethodParameter createByAnnotation(Annotation annotation) {
        if (annotation.annotationType() == ClientId.class) {
            return new ClientIdParameter();
        }
        if (annotation.annotationType() == Token.class) {
            return new TokenParamter();
        }
        if (annotation.annotationType() == UserId.class) {
            return new UserIdParameter();
        }
        throw new IllegalStateException();
    }

    private MethodParameter createModelParameter(Parameter parameter) {
        Model modelAnnotation = AnnotationUtils.getAnnotationOrThrow(parameter.getType(), Model.class);
        String id = modelAnnotation.value().isEmpty() ? parameter.getType().getName() : modelAnnotation.value();
        return new ModelParameter(id, parameter.getType());
    }

}
