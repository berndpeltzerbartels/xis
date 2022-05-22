package one.xis.context;

import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
class SimpleDependencyField implements DependencyField {
    private final Set<Object> owners = new HashSet<>();
    private Object fieldValue;
    private final Field field;

    @Override
    public void onComponentCreated(Object o) {
        if (field.getDeclaringClass().isInstance(o)) {
            owners.add(o);
        }
        if (field.getType().isInstance(o)) {
            if (fieldValue != null) {
                throw new AppContextException("ambigious candidates for " + field);
            }
            fieldValue = o;
        }
    }

    @Override
    public void doInjection() {
        if (fieldValue == null) {
            throw new AppContextException("no candidate for " + field);
        }
        if (owners.isEmpty()) {
            throw new IllegalStateException();
        }
        owners.forEach(owner -> inject(owner, field, fieldValue));
    }

}
