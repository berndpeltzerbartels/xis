package one.xis.context;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.Collection;

@UtilityClass
class DependencyFields {


    DependencyField createField(Field f, SingletonWrapper parent) {
        if (Collection.class.isAssignableFrom(f.getType())) {
            return new CollectionDependencyField(f, parent);
        }
        if (f.getType().isArray()) {
            return new ArrayDependencyField(f, parent);
        }
        boolean optional = false;
        if (f.isAnnotationPresent(XISInject.class)) {
            optional = f.getAnnotation(XISInject.class).optional();
        }
        return new SimpleDependencyField(f, parent, optional);
    }
}
