package one.xis.mongodb;

import java.lang.reflect.ParameterizedType;

class MongoRepositoryMetadata {
    private final Class<?> entityType;
    private final Class<?> idType;

    MongoRepositoryMetadata(Class<?> repositoryInterface) {
        ParameterizedType type = crudRepositoryType(repositoryInterface);
        this.entityType = (Class<?>) type.getActualTypeArguments()[0];
        this.idType = (Class<?>) type.getActualTypeArguments()[1];
    }

    Class<?> entityType() {
        return entityType;
    }

    Class<?> idType() {
        return idType;
    }

    private ParameterizedType crudRepositoryType(Class<?> repositoryInterface) {
        for (var type : repositoryInterface.getGenericInterfaces()) {
            if (type instanceof ParameterizedType parameterizedType
                    && parameterizedType.getRawType().equals(MongoCrudRepository.class)) {
                return parameterizedType;
            }
        }
        throw new IllegalArgumentException(repositoryInterface.getName() + " must extend MongoCrudRepository<E, ID>");
    }
}
