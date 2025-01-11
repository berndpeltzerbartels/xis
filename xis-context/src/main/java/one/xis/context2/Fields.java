package one.xis.context2;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

// test
class Fields {
    private final List<SingletonField> fields = new ArrayList<>();

    SingletonField createField(Field f, SingletonWrapper parent) {
        var field = new SingletonField(f, parent);
        fields.add(field);
        return field;
    }
}
