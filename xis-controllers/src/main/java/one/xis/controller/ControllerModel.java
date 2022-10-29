package one.xis.controller;

import lombok.Builder;
import lombok.Getter;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.stream.Stream;

@Builder
@Getter
public class ControllerModel {
    private final Class<?> controllerClass;
    private final Collection<ControllerMethod> controllerMethods;
    private final Class<?> modelType;

    public <A extends Annotation> Stream<ControllerMethod> getAnnotatedMethods(Class<A> annotationClass) {
        return controllerMethods.stream().filter(controllerMethod -> controllerMethod.annotation(annotationClass).isPresent());
    }

    public String getControllerClassName() {
        return controllerClass.getName(); // TODO Proxies ?
    }
}
