package one.xis.context;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.util.Collection;

public interface DependencyField {
    void onComponentCreated(Object o);

    void doInjection();

    @SneakyThrows
    default void inject(Object owner, Field field, Object fieldValue) {
        field.setAccessible(true);
        field.set(owner, fieldValue);
    }

    static DependencyField getWrapperInstance(Field field) {
        if (Collection.class.isAssignableFrom(field.getType())) {
            return new CollectionDependencyField(field);
        }
        if (field.getType().isArray()) {
            return new ArrayDependencyField(field);
        }
        return new SimpleDependencyField(field);
    }
}
