package one.xis.mongodb;

import one.xis.utils.lang.FieldUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.List;

class MongoDocumentMetadata {
    private final Class<?> type;
    private final List<Property> properties;
    private final Property idProperty;

    MongoDocumentMetadata(Class<?> type) {
        this.type = type;
        this.properties = type.isRecord() ? recordProperties(type) : fieldProperties(type);
        this.idProperty = properties.stream()
                .filter(Property::id)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No Mongo id property on " + type.getName()));
    }

    String collectionName() {
        MongoDocument annotation = type.getAnnotation(MongoDocument.class);
        if (annotation == null) {
            throw new IllegalStateException(type.getName() + " is not annotated with @MongoDocument");
        }
        return annotation.value();
    }

    List<Property> properties() {
        return properties;
    }

    Property idProperty() {
        return idProperty;
    }

    private List<Property> fieldProperties(Class<?> type) {
        return FieldUtil.getFields(type, this::persistentField).stream()
                .map(FieldProperty::new)
                .map(Property.class::cast)
                .toList();
    }

    private boolean persistentField(Field field) {
        return !Modifier.isStatic(field.getModifiers())
                && !Modifier.isTransient(field.getModifiers())
                && !field.isAnnotationPresent(MongoIgnore.class);
    }

    private List<Property> recordProperties(Class<?> type) {
        return List.of(type.getRecordComponents()).stream()
                .filter(component -> !component.isAnnotationPresent(MongoIgnore.class))
                .map(RecordProperty::new)
                .map(Property.class::cast)
                .toList();
    }

    interface Property {
        String name();

        String documentName();

        Class<?> type();

        Object get(Object object);

        boolean id();
    }

    private record FieldProperty(Field field) implements Property {
        @Override
        public String name() {
            return field.getName();
        }

        @Override
        public String documentName() {
            if (id()) {
                return "_id";
            }
            MongoField annotation = field.getAnnotation(MongoField.class);
            return annotation == null ? field.getName() : annotation.value();
        }

        @Override
        public Class<?> type() {
            return field.getType();
        }

        @Override
        public Object get(Object object) {
            return FieldUtil.getFieldValue(object, field);
        }

        @Override
        public boolean id() {
            return field.isAnnotationPresent(MongoId.class) || field.getName().equals("id");
        }
    }

    private record RecordProperty(RecordComponent component) implements Property {
        @Override
        public String name() {
            return component.getName();
        }

        @Override
        public String documentName() {
            if (id()) {
                return "_id";
            }
            MongoField annotation = component.getAnnotation(MongoField.class);
            return annotation == null ? component.getName() : annotation.value();
        }

        @Override
        public Class<?> type() {
            return component.getType();
        }

        @Override
        public Object get(Object object) {
            try {
                return component.getAccessor().invoke(object);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Could not read record component " + component.getName(), e);
            }
        }

        @Override
        public boolean id() {
            return component.isAnnotationPresent(MongoId.class) || component.getName().equals("id");
        }
    }
}
