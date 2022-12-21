package one.xis.utils.reflect;

import lombok.NonNull;
import lombok.experimental.UtilityClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Optional;
import java.util.Set;

@UtilityClass
public class AnnotationUtils {

    public <A extends Annotation> A getAnnotationOrThrow(@NonNull AnnotatedElement annotatedType, @NonNull Class<A> annotationClass) {
        return getAnnotation(annotatedType, annotationClass).orElseThrow(() -> new IllegalStateException(annotatedType + " must be annotated with " + annotationClass.getSimpleName()));
    }


    public <A extends Annotation> Optional<A> getAnnotation(@NonNull AnnotatedElement annotatedType, @NonNull Class<A> annotationClass) {
        if (!annotatedType.isAnnotationPresent(annotationClass)) {
            return Optional.empty();
        }
        return Optional.of(annotatedType.getAnnotation(annotationClass));
    }

    public boolean hasAtLeasOneAnnotation(AnnotatedElement element, Set<Class<? extends Annotation>> annotations) {
        return annotations.stream().anyMatch(element::isAnnotationPresent);
    }


}
