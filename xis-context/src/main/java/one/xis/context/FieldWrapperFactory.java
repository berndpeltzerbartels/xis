package one.xis.context;

import lombok.RequiredArgsConstructor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

@RequiredArgsConstructor
class FieldWrapperFactory {

    private final List<Class<?>> allComponentClasses;

    FieldWrapper createFieldWrapper(Field field, Object owner) {
        if (field.getType().isArray()) {
            return new ArrayFieldWrapper(field, owner, fieldAnnotation(field), allComponentClasses);

        } else if (Collection.class.isAssignableFrom(field.getType())) {
            return new CollectionFieldWrapper(field, owner, fieldAnnotation(field), allComponentClasses);
        } else {
            return new SimpleFieldWrapper(field, owner, fieldAnnotation(field));
        }
    }

    private Class<? extends Annotation> fieldAnnotation(Field field) {
        return switch (field.getAnnotations().length) {
            case 0 -> null;
            case 1 -> field.getAnnotations()[0].annotationType();
            default -> throw new IllegalStateException("too many annotations: " + field);
        };
    }
}
