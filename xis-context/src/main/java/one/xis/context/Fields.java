package one.xis.context;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;

@UtilityClass
class Fields {


    DependencyField createField(Field f, SingletonWrapper parent) {
        var field = new SimpleDependencyField(f, parent);
        return field;
    }
}
