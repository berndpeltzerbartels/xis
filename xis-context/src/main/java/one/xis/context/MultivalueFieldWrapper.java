package one.xis.context;

import lombok.NonNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;


abstract class MultivalueFieldWrapper implements FieldWrapper {

    protected final Field field;
    protected final Object owner;
    private final Class<? extends Annotation> fieldAnnotation;
    private final List<Class<?>> candidateClasses;
    private final Collection<Object> values = new HashSet<>();

    MultivalueFieldWrapper(Field field,
                           Object owner,
                           Class<? extends Annotation> fieldAnnotation,
                           Collection<Class<?>> allClasses) {
        this.field = field;
        this.owner = owner;
        this.fieldAnnotation = fieldAnnotation;
        this.candidateClasses = allClasses.stream()
                .filter(field.getType()::isAssignableFrom)
                .filter(this::annonationMatches)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isInjected() {
        return candidateClasses.isEmpty();
    }

    @Override
    public void onComponentCreated(Object o) {
        if (field.getType().isInstance(o) && annonationMatches(o.getClass())) {
            candidateClasses.remove(o.getClass());
            values.add(o);
        }
        if (candidateClasses.isEmpty()) {
            setFieldValue(field, values);
        }
    }

    protected abstract void setFieldValue(Field field, Collection<Object> values);

    boolean annonationMatches(@NonNull Class<?> clazz) {
        if (fieldAnnotation == null) {
            return true;
        }
        var c = clazz;
        while (c != null) {
            if (c.isAnnotationPresent(fieldAnnotation)) {
                return true;
            }
            c = c.getSuperclass();
        }
        return Arrays.stream(clazz.getInterfaces()).anyMatch(interf -> interf.isAnnotationPresent(fieldAnnotation)); // TODO Superinterfaces
    }


}
