package one.xis.sql;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

class RepositoryMetadata {
    private final Class<?> repositoryInterface;
    private final Class<?> entityType;
    private final Class<?> idType;

    RepositoryMetadata(Class<?> repositoryInterface) {
        this.repositoryInterface = repositoryInterface;
        Type[] types = crudTypes(repositoryInterface);
        this.entityType = (Class<?>) types[0];
        this.idType = (Class<?>) types[1];
    }

    Class<?> repositoryInterface() {
        return repositoryInterface;
    }

    Class<?> entityType() {
        return entityType;
    }

    Class<?> idType() {
        return idType;
    }

    private Type[] crudTypes(Class<?> type) {
        for (Type genericInterface : type.getGenericInterfaces()) {
            Type[] result = crudTypes(genericInterface);
            if (result != null) {
                return result;
            }
        }
        if (type.getSuperclass() != null && !type.getSuperclass().equals(Object.class)) {
            return crudTypes(type.getSuperclass());
        }
        throw new IllegalArgumentException(repositoryInterface.getName()
                + " must extend CrudRepository<E, ID> to use generic repository methods");
    }

    private Type[] crudTypes(Type type) {
        if (type instanceof ParameterizedType parameterizedType) {
            if (parameterizedType.getRawType().equals(CrudRepository.class)) {
                return parameterizedType.getActualTypeArguments();
            }
            if (parameterizedType.getRawType() instanceof Class<?> rawType) {
                return crudTypes(rawType);
            }
        }
        if (type instanceof Class<?> clazz) {
            return crudTypes(clazz);
        }
        return null;
    }
}
