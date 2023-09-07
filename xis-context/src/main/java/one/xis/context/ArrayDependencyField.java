package one.xis.context;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ArrayDependencyField extends DependencyField {
    private final Set<Object> owners = new HashSet<>();
    private final List<Object> fieldValues;

    ArrayDependencyField(Field field) {
        super(field, elementType(field));
        this.fieldValues = new ArrayList<>();
    }

    private static Class<?> elementType(Field field) {
        return field.getType().getComponentType();
    }

    @Override
    public void onComponentCreated(Object o) {
        if (field.getDeclaringClass().isInstance(o)) {
            owners.add(o);
        }
        if (isMatchingFieldValue(o)) {
            fieldValues.add(o);
        }
    }

    @Override
    public void doInjection() {
        var arr = (Object[]) Array.newInstance(elementType, fieldValues.size());
        owners.forEach(owner -> inject(owner, field, fieldValues.toArray(arr)));
    }
}
