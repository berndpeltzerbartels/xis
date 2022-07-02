package one.xis.context;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;


class SimpleDependencyField extends DependencyField {
    private final Set<Object> owners = new HashSet<>();
    private Object fieldValue;

    SimpleDependencyField(Field field) {
        super(field, field.getType());
    }

    @Override
    public void onComponentCreated(Object o) {
        if (field.getDeclaringClass().isInstance(o)) {
            owners.add(o);
        }
        if (isMatchingFieldValue(o)) {
            if (fieldValue != null) {
                throw new AppContextException("ambigious candidates for " + field);
            }
            fieldValue = o;
        }
    }

    @Override
    public void doInjection() {
        if (fieldValue == null) {
            throw new AppContextException("unsatisfied dependency in " + field);
        }
        if (owners.isEmpty()) {
            throw new IllegalStateException();
        }
        owners.forEach(owner -> inject(owner, field, fieldValue));
    }

}
