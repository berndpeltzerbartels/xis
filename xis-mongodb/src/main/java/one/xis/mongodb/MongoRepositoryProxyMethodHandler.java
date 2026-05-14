package one.xis.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import one.xis.context.ProxyMethodHandler;
import org.bson.Document;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

class MongoRepositoryProxyMethodHandler extends ProxyMethodHandler {
    private final Map<Method, MongoMethodHandler> handlers = new HashMap<>();

    MongoRepositoryProxyMethodHandler(MongoDatabase database, Class<?> repositoryInterface) {
        var metadata = new MongoRepositoryMetadata(repositoryInterface);
        var documentMetadata = new MongoDocumentMetadata(metadata.entityType());
        MongoCollection<Document> collection = database.getCollection(documentMetadata.collectionName());
        var mapper = new MongoMapper();
        for (Method method : repositoryInterface.getMethods()) {
            MongoMethodHandler handler = handler(method, collection, mapper, metadata);
            if (handler != null) {
                handlers.put(method, handler);
            }
        }
    }

    @Override
    protected Object doInvoke(Object proxy, Method method, Object[] args) {
        MongoMethodHandler handler = handlers.get(method);
        if (handler == null) {
            throw new UnsupportedOperationException("No Mongo repository handler for method " + method);
        }
        return handler.invoke(args == null ? new Object[0] : args);
    }

    private MongoMethodHandler handler(Method method, MongoCollection<Document> collection, MongoMapper mapper,
                                       MongoRepositoryMetadata metadata) {
        if (method.isAnnotationPresent(MongoQuery.class)) {
            return new MongoQueryMethodHandler(collection, mapper, metadata.entityType(), method);
        }
        if (!method.getDeclaringClass().equals(MongoCrudRepository.class)) {
            return null;
        }
        return switch (method.getName()) {
            case "findById" -> args -> java.util.Optional.ofNullable(
                    mapper.toObject(collection.find(idFilter(mapper, args[0])).first(), metadata.entityType()));
            case "findAll" -> args -> mapper.toObjects(collection.find(), metadata.entityType());
            case "save" -> args -> save(collection, mapper, args[0]);
            case "delete" -> args -> collection.deleteOne(idFilter(mapper,
                    new MongoDocumentMetadata(metadata.entityType()).idProperty().get(args[0]))).getDeletedCount() > 0;
            case "deleteById" -> args -> collection.deleteOne(idFilter(mapper, args[0])).getDeletedCount() > 0;
            case "count" -> args -> collection.countDocuments();
            default -> null;
        };
    }

    private Document idFilter(MongoMapper mapper, Object id) {
        if (id == null) {
            throw new IllegalArgumentException("Mongo id must not be null");
        }
        return new Document("_id", mapper.idValue(id, id.getClass()));
    }

    private Object save(MongoCollection<Document> collection, MongoMapper mapper, Object entity) {
        var metadata = new MongoDocumentMetadata(entity.getClass());
        Object id = metadata.idProperty().get(entity);
        Document document = mapper.toDocument(entity);
        collection.replaceOne(idFilter(mapper, id), document, new com.mongodb.client.model.ReplaceOptions().upsert(true));
        return entity;
    }

    private interface MongoMethodHandler {
        Object invoke(Object[] args);
    }

    private static class MongoQueryMethodHandler implements MongoMethodHandler {
        private final MongoCollection<Document> collection;
        private final MongoMapper mapper;
        private final Class<?> entityType;
        private final Method method;

        private MongoQueryMethodHandler(MongoCollection<Document> collection, MongoMapper mapper,
                                        Class<?> entityType, Method method) {
            this.collection = collection;
            this.mapper = mapper;
            this.entityType = entityType;
            this.method = method;
        }

        @Override
        public Object invoke(Object[] args) {
            Document query = Document.parse(bind(method.getAnnotation(MongoQuery.class).value(), args));
            Class<?> returnType = method.getReturnType();
            if (java.util.Optional.class.isAssignableFrom(returnType)) {
                return java.util.Optional.ofNullable(mapper.toObject(collection.find(query).first(), genericReturnType()));
            }
            if (java.util.Collection.class.isAssignableFrom(returnType)) {
                return mapper.toObjects(collection.find(query), genericReturnType());
            }
            return mapper.toObject(collection.find(query).first(), entityType);
        }

        private String bind(String query, Object[] args) {
            String result = query;
            for (int i = 0; i < args.length; i++) {
                result = result.replace("?" + i, toJson(args[i]));
            }
            return result;
        }

        private String toJson(Object value) {
            if (value == null) {
                return "null";
            }
            if (value instanceof Number || value instanceof Boolean) {
                return value.toString();
            }
            return "\"" + value.toString().replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
        }

        private Class<?> genericReturnType() {
            if (!(method.getGenericReturnType() instanceof ParameterizedType parameterizedType)) {
                throw new IllegalArgumentException("@MongoQuery collection/optional method needs generic return type: " + method);
            }
            return (Class<?>) parameterizedType.getActualTypeArguments()[0];
        }
    }
}
