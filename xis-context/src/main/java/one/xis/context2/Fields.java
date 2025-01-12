package one.xis.context2;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
class Fields {


    SingletonField createField(Field f, SingletonWrapper parent) {
        var field = new SingletonField(f, parent);
        return field;
    }
}
