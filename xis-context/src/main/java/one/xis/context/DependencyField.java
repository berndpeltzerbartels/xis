package one.xis.context;

import lombok.SneakyThrows;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Optional;

public abstract class DependencyField implements ComponentCreationListener {


    protected final Field field;
    protected final Class<?> elementType;

    protected DependencyField(Field field, Class<?> elementType) {
        this.field = field;
        this.elementType = elementType;
        validateAnnotationToFilter();
    }

    abstract void doInjection();

    @SneakyThrows
    protected void inject(Object owner, Field field, Object fieldValue) {
        field.setAccessible(true);
        field.set(owner, fieldValue);
    }

    protected boolean isMatchingFieldValue(Object o) {
        if (!elementType.isInstance(o)) {
            return false;
        }
        return annotationFilterMatches(o);
    }


    private boolean annotationFilterMatches(Object o) {
        return getAnnotationToFilter().map(c -> o.getClass().isAnnotationPresent(c)).orElse(true);
    }

    private void validateAnnotationToFilter() {
        getAnnotationToFilter().ifPresent(this::validateAnnotationToFilter);
    }

    private <A extends Annotation> void validateAnnotationToFilter(Class<A> anno) {
        if (!anno.isAnnotationPresent(XISComponent.class)) {
            String annoName = anno.getSimpleName();
            String componentAnnoName = XISComponent.class.getSimpleName();
            throw new AppContextException(String.format("%s used in @XISInject is not annotated as component. Consider to annotate @%s with @%s", annoName, annoName, componentAnnoName));
        }
    }

    private Optional<Class<? extends Annotation>> getAnnotationToFilter() {
        XISInject inject = field.getAnnotation(XISInject.class);
        if (inject.annotatedWith().equals(None.class)) {
            return Optional.empty();
        }
        return Optional.of(inject.annotatedWith());
    }

    static DependencyField getInstanceForField(Field field) {
        if (Collection.class.isAssignableFrom(field.getType())) {
            return new CollectionDependencyField(field);
        }
        if (field.getType().isArray()) {
            return new ArrayDependencyField(field);
        }
        return new SimpleDependencyField(field);
    }

    static DependencyField getInstanceForObject(Field field, Object owner) {
        var dependencyField = getInstanceForField(field);
        dependencyField.onComponentCreated(owner);
        return dependencyField;
    }
}
