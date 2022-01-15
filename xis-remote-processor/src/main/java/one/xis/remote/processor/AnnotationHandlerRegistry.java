package one.xis.remote.processor;

import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.stream.Stream;

public class AnnotationHandlerRegistry {

    static Stream<CodeWriter> getHandlerForTypeAnnotation(TypeElement annotationType) {
        return null;
    }

    static Stream<CodeWriter> getHandlerForFieldAnnotation(TypeElement annotationType) {
        return null;
    }


    static Stream<CodeWriter> getHandlerForMethodAnnotation(TypeElement annotationType) {
        return null;
    }

    static Stream<CodeWriter> getHandlerForParamAnnotation(TypeElement annotationType) {
        return null;
    }


    static Stream<TypeElement> getTypeAnnotations() {
        return null;
    }

    static Stream<TypeElement> getFieldAnnotations() {
        return null;
    }

    static Stream<TypeElement> getMethodAnnotations() {
        return null;
    }

    static Stream<TypeElement> getParamAnnotations() {
        return null;
    }

    static <T extends Annotation, F extends Annotation> Stream<Class<F>> getFieldAnnotations(Class<T> typeAnnotations) {
        return null;
    }
}
