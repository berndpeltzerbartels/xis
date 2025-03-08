package one.xis.context;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;

@UtilityClass
class Fields {


    SimpleField createField(Field f, SingletonWrapper parent) {
        var field = new SimpleField(f, parent);
        return field;
    }
}
