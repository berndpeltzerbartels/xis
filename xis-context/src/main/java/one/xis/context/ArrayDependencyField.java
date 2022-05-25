package one.xis.context;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class ArrayDependencyField implements DependencyField {
    private final Set<Object> owners = new HashSet<>();
    private final List<Object> fieldValues;
    private final Class<?> elementType;
    private final Field field;

    ArrayDependencyField(Field field) {
        this.field = field;
        this.fieldValues = new ArrayList<>();
        elementType = elementType(field);
    }

    Class<?> elementType(Field field) {
        return field.getType().getComponentType();
    }

    @Override
    public void onComponentCreated(Object o) {
        if (field.getDeclaringClass().isInstance(o)) {
            owners.add(o);
        }
        if (elementType.isInstance(o)) {
            fieldValues.add(o);
        }
    }

    @Override
    public void doInjection() {
        Object[] arr = (Object[]) Array.newInstance(elementType, fieldValues.size());
        owners.forEach(owner -> inject(owner, field, fieldValues.toArray(arr)));
    }
}
