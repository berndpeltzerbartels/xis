package one.xis.sql;

import one.xis.utils.lang.FieldUtil;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.util.List;

class EntitySQLMapping {

    private final Class<?> type;
    private final String tableName;
    private final List<Property> properties;

    EntitySQLMapping(Class<?> type) {
        this.type = type;
        Entity entity = type.getAnnotation(Entity.class);
        if (entity == null) {
            throw new IllegalArgumentException("@Save parameter type must be annotated with @Entity: " + type.getName());
        }
        this.tableName = entity.value();
        this.properties = type.isRecord() ? recordProperties(type) : fieldProperties(type);
    }

    String tableName() {
        return tableName;
    }

    Class<?> type() {
        return type;
    }

    List<Property> properties() {
        return properties;
    }

    Property propertyByColumn(String columnName) {
        return properties.stream()
                .filter(property -> normalize(property.columnName()).equals(normalize(columnName)))
                .findFirst()
                .orElse(null);
    }

    Property propertyByNameOrColumn(String name) {
        return properties.stream()
                .filter(property -> property.name().equals(name) || normalize(property.columnName()).equals(normalize(name)))
                .findFirst()
                .orElse(null);
    }

    private List<Property> fieldProperties(Class<?> type) {
        return FieldUtil.getFields(type, field -> mappedField(type, field)).stream()
                .map(field -> new FieldProperty(field.getName(), columnName(field), optionalColumn(type, field), field))
                .map(Property.class::cast)
                .toList();
    }

    private boolean mappedField(Class<?> ownerType, Field field) {
        return !Modifier.isStatic(field.getModifiers())
                && !Modifier.isTransient(field.getModifiers())
                && !SQLAnnotationSupport.ignored(ownerType, field)
                && columnMappedType(ownerType, field);
    }

    private List<Property> recordProperties(Class<?> type) {
        return List.of(type.getRecordComponents()).stream()
                .filter(component -> !SQLAnnotationSupport.ignored(component))
                .filter(this::columnMappedType)
                .map(component -> new RecordProperty(component.getName(), columnName(component),
                        SQLAnnotationSupport.optionalColumn(component), component))
                .map(Property.class::cast)
                .toList();
    }

    private boolean columnMappedType(Class<?> ownerType, Field field) {
        return ROMapper.isSimpleType(field.getType()) || SQLAnnotationSupport.jsonColumn(ownerType, field);
    }

    private boolean columnMappedType(RecordComponent component) {
        return ROMapper.isSimpleType(component.getType()) || SQLAnnotationSupport.jsonColumn(component);
    }

    private String columnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        return column == null ? camelToSnake(field.getName()) : column.value();
    }

    private String columnName(RecordComponent component) {
        Column column = component.getAnnotation(Column.class);
        return column == null ? camelToSnake(component.getName()) : column.value();
    }

    private String camelToSnake(String value) {
        var result = new StringBuilder(value.length() + 4);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append('_');
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    private String normalize(String value) {
        return value == null ? "" : value.replace("_", "").toLowerCase();
    }

    private boolean optionalColumn(Class<?> ownerType, Field field) {
        return SQLAnnotationSupport.optionalColumn(ownerType, field);
    }

    interface Property {
        String name();

        String columnName();

        default boolean optionalColumn() {
            return false;
        }

        Object get(Object object);
    }

    private record FieldProperty(String name, String columnName, boolean optionalColumn, Field field) implements Property {

        @Override
        public Object get(Object object) {
            return FieldUtil.getFieldValue(object, field);
        }
    }

    private record RecordProperty(String name, String columnName, boolean optionalColumn,
                                  RecordComponent component) implements Property {

        @Override
        public Object get(Object object) {
            try {
                return component.getAccessor().invoke(object);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException("Could not read record component " + component.getName(), e);
            }
        }
    }
}
