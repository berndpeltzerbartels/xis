package one.xis.deserialize;

import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@RequiredArgsConstructor
class TypeTarget implements AnnotatedElement {

    private final Type type;
    private final AnnotatedElement annotationSource;

    Type getType() {
        return type;
    }

    Class<?> getRawType() {
        if (type instanceof Class<?> clazz) {
            return clazz;
        }
        if (type instanceof ParameterizedType parameterizedType) {
            return (Class<?>) parameterizedType.getRawType();
        }
        throw new IllegalArgumentException("Unsupported type: " + type);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        return annotationSource.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return annotationSource.getAnnotations();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return annotationSource.getDeclaredAnnotations();
    }
}
