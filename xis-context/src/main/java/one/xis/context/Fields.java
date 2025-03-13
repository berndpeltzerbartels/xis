package one.xis.context;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.Collection;

@UtilityClass
class Fields {


    DependencyField createField(Field f, SingletonWrapper parent) {
        if (Collection.class.isAssignableFrom(f.getType())) {
            return new CollectionDependencyField(f, parent);
        }
        if (f.getType().isArray()) {
            return new ArrayDependencyField(f, parent);
        }
        return new SimpleDependencyField(f, parent);
    }
}
