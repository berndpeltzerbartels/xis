package one.xis.context;

import lombok.SneakyThrows;

import java.lang.reflect.*;
import java.util.*;

class CollectionDependencyField extends DependencyField {
    private final Set<Object> owners = new HashSet<>();
    private final Collection<Object> fieldValues;

    @SuppressWarnings({"unchecked", "rawtypes"})
    CollectionDependencyField(Field field) {
        super(field, actualTypeParameter(field));
        this.fieldValues = createCollection((Class<? extends Collection>) field.getType());
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

    private static Class<?> actualTypeParameter(Field field) {
        if (!(field.getGenericType() instanceof ParameterizedType)) {
            throw new AppContextException(field + ": collection-dependency-fields must have generic type parameter");
        }
        ParameterizedType collType = (ParameterizedType) field.getGenericType();
        Type actualType = collType.getActualTypeArguments()[0];
        if (actualType instanceof WildcardType) {
            // TODO this will not always work
            WildcardType wildcardType = (WildcardType) actualType;
            return (Class<?>) wildcardType.getUpperBounds()[0];

        } else if (actualType instanceof Class) {
            return (Class<?>) collType.getActualTypeArguments()[0];
        } else if (actualType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) actualType;
            return (Class<?>) parameterizedType.getRawType();
        }
        throw new IllegalStateException(); // should never happen
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
        owners.forEach(owner -> inject(owner, field, fieldValues));
    }

}
