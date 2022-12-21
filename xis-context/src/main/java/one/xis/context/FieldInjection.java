package one.xis.context;

import lombok.Getter;
import lombok.NonNull;
import one.xis.utils.lang.FieldUtil;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
class FieldInjection {
    private final Set<DependencyField> dependencyFields;

    FieldInjection(Reflection reflection, Collection<Class<?>> additionalClasses, Collection<Object> additionalBeans) {
        dependencyFields = new HashSet<>();
        dependencyFields.addAll(dependencyFieldsByReflection(reflection));
        dependencyFields.addAll(dependencyFieldsOfClasses(additionalClasses));
        dependencyFields.addAll(dependencyFieldsOfObjects(additionalBeans));
    }

    private Collection<DependencyField> dependencyFieldsByReflection(Reflection reflection) {
        return reflection.getDependencyFields().stream()
                .map(DependencyField::getInstanceForField)
                .collect(Collectors.toSet());
    }

    private Collection<DependencyField> dependencyFieldsOfClasses(Collection<Class<?>> classes) {
        return classes.stream().flatMap(this::dependencyFieldsOfClass).collect(Collectors.toSet());
    }

    private Stream<DependencyField> dependencyFieldsOfClass(Class<?> c) {
        return FieldUtil.getAllFields(c).stream()
                .filter(this::isDependencyField)
                .map(DependencyField::getInstanceForField);
    }

    private Collection<DependencyField> dependencyFieldsOfObjects(Collection<Object> objects) {
        return objects.stream().flatMap(this::dependencyFieldsOfObject).collect(Collectors.toSet());
    }

    private Stream<DependencyField> dependencyFieldsOfObject(@NonNull Object o) {
        return FieldUtil.getAllFields(o.getClass()).stream()
                .filter(this::isDependencyField)
                .map(field -> DependencyField.getInstanceForObject(field, o));
    }

    private boolean isDependencyField(Field field) {
        return field.isAnnotationPresent(XISInject.class);
    }

    void onComponentCreated(Object o) {
        dependencyFields.forEach(field -> field.onComponentCreated(o));
    }

    void doInjection() {
        dependencyFields.forEach(DependencyField::doInjection);
    }
}
