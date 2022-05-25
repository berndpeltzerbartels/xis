package one.xis.context;

import lombok.SneakyThrows;

import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.util.*;

class CollectionParameter extends MultiValueParameter {

    private final Class<? extends Collection<Object>> collType;

    @SuppressWarnings("unchecked")
    CollectionParameter(Parameter parameter) {
        super(parameter);
        this.collType = (Class<? extends Collection<Object>>) parameter.getType();
    }

    @Override
    protected Class<?> findElementType(Parameter parameter) {
        if (!ParameterizedType.class.isInstance(parameter.getParameterizedType())) {
            throw new AppContextException(parameter + ": collection-dependency-parameter must have generic type parameter");
        }
        ParameterizedType collType = (ParameterizedType) parameter.getParameterizedType();
        return (Class<?>) collType.getActualTypeArguments()[0];
    }

    @Override
    Object getValue() {
        return createCollection(collType, getValues());
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    <C extends Collection<Object>> C createCollection(Class<C> collType, Collection<Object> values) {
        if (!collType.isInterface() && !Modifier.isAbstract(collType.getModifiers())) {
            return collType.getConstructor().newInstance(values);
        }
        if (collType.equals(Set.class) || collType.equals(Collection.class)) {
            return (C) new HashSet<>(values);
        }
        if (collType.equals(List.class)) {
            return (C) new LinkedList<>(values);
        }
        throw new AppContextException("unsupported field type: " + collType);
    }

}
