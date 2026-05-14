package one.xis.mongodb;

import one.xis.utils.lang.ClassUtils;
import one.xis.utils.lang.FieldUtil;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.List;
import java.util.UUID;

class MongoMapper {

    Document toDocument(Object entity) {
        var metadata = new MongoDocumentMetadata(entity.getClass());
        var document = new Document();
        for (MongoDocumentMetadata.Property property : metadata.properties()) {
            Object value = property.get(entity);
            if (value != null) {
                document.put(property.documentName(), toMongoValue(value));
            }
        }
        return document;
    }

    <T> T toObject(Document document, Class<T> type) {
        if (document == null) {
            return null;
        }
        var metadata = new MongoDocumentMetadata(type);
        if (type.isRecord()) {
            return toRecord(document, type, metadata);
        }
        T object = ClassUtils.newInstance(type);
        for (MongoDocumentMetadata.Property property : metadata.properties()) {
            Object value = fromMongoValue(document.get(property.documentName()), property.type());
            FieldUtil.setFieldValue(object, field(type, property.name()), valueOrDefault(property.type(), value));
        }
        return object;
    }

    Object idValue(Object id, Class<?> targetType) {
        return toMongoValue(id);
    }

    private Object toMongoValue(Object value) {
        if (value instanceof UUID uuid) {
            return uuid.toString();
        }
        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        return value;
    }

    private Object fromMongoValue(Object value, Class<?> targetType) {
        if (value == null || targetType.isInstance(value)) {
            return value;
        }
        if (targetType == String.class && value instanceof ObjectId objectId) {
            return objectId.toHexString();
        }
        if (targetType == ObjectId.class) {
            return new ObjectId(value.toString());
        }
        if (targetType == UUID.class) {
            return UUID.fromString(value.toString());
        }
        if (targetType.isEnum()) {
            @SuppressWarnings({"unchecked", "rawtypes"})
            Object enumValue = Enum.valueOf((Class<? extends Enum>) targetType.asSubclass(Enum.class), value.toString());
            return enumValue;
        }
        if (targetType == int.class || targetType == Integer.class) {
            return ((Number) value).intValue();
        }
        if (targetType == long.class || targetType == Long.class) {
            return ((Number) value).longValue();
        }
        if (targetType == double.class || targetType == Double.class) {
            return ((Number) value).doubleValue();
        }
        if (targetType == float.class || targetType == Float.class) {
            return ((Number) value).floatValue();
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return value;
        }
        return value;
    }

    private <T> T toRecord(Document document, Class<T> type, MongoDocumentMetadata metadata) {
        try {
            RecordComponent[] components = type.getRecordComponents();
            Object[] args = new Object[components.length];
            Class<?>[] parameterTypes = new Class<?>[components.length];
            for (int i = 0; i < components.length; i++) {
                RecordComponent component = components[i];
                parameterTypes[i] = component.getType();
                MongoDocumentMetadata.Property property = metadata.properties().stream()
                        .filter(p -> p.name().equals(component.getName()))
                        .findFirst()
                        .orElse(null);
                args[i] = property == null ? defaultValue(component.getType())
                        : valueOrDefault(component.getType(), fromMongoValue(document.get(property.documentName()), component.getType()));
            }
            Constructor<T> constructor = type.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Could not create Mongo record " + type.getName(), e);
        }
    }

    private java.lang.reflect.Field field(Class<?> type, String name) {
        return FieldUtil.getFields(type, field -> field.getName().equals(name)).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No field " + name + " on " + type.getName()));
    }

    private Object valueOrDefault(Class<?> type, Object value) {
        return value == null && type.isPrimitive() ? defaultValue(type) : value;
    }

    private Object defaultValue(Class<?> type) {
        if (type == boolean.class) return false;
        if (type == byte.class) return (byte) 0;
        if (type == short.class) return (short) 0;
        if (type == int.class) return 0;
        if (type == long.class) return 0L;
        if (type == float.class) return 0f;
        if (type == double.class) return 0d;
        if (type == char.class) return '\0';
        return null;
    }

    <T> List<T> toObjects(Iterable<Document> documents, Class<T> type) {
        var result = new java.util.ArrayList<T>();
        for (Document document : documents) {
            result.add(toObject(document, type));
        }
        return result;
    }
}
