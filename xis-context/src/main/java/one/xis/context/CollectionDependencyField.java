package one.xis.context;

import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.*;

class CollectionDependencyField implements DependencyField {
    private final Set<Object> owners = new HashSet<>();
    private final Collection<Object> fieldValues;
    private final Class<?> elementType;
    private final Field field;

    @SuppressWarnings("unchecked")
    CollectionDependencyField(Field field) {
        this.field = field;
        this.fieldValues = createCollection((Class<? extends Collection>) field.getType());
        elementType = actualTypeParameter(field);
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    <C extends Collection<Object>> C createCollection(Class<C> collType) {
        if (!collType.isInterface() && !Modifier.isAbstract(collType.getModifiers())) {
            return collType.getConstructor().newInstance();
        }
        if (collType.equals(Set.class) || collType.equals(Collection.class)) {
            return (C) new HashSet<>();
        }
        if (collType.equals(List.class)) {
            return (C) new LinkedList<>();
        }
        throw new AppContextException("unsupported field type: " + collType);
    }

    private Class<?> actualTypeParameter(Field field) {
        if (!ParameterizedType.class.isInstance(field.getGenericType())) {
            throw new AppContextException(field + ": collection-dependency-fields must have generic type parameter");
        }
        ParameterizedType collType = (ParameterizedType) field.getGenericType();
        return (Class<?>) collType.getActualTypeArguments()[0];
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
        owners.forEach(owner -> inject(owner, field, fieldValues));
    }

}
