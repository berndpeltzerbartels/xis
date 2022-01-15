package one.xis.remote.processor;

import lombok.NonNull;

import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;

class AnnotationUtils {

    @NonNull
    static <A extends Annotation> A requiredAnnotation(Class<?> c, Class<A> annotation) {

        return null;
    }

    @NonNull
    static <A extends Annotation> A requiredAnnotation(TypeElement type, TypeElement annotation) {

        return null;
    }
}
