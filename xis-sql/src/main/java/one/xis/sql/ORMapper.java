package one.xis.sql;

import lombok.NonNull;
import one.xis.utils.lang.FieldUtil;
import one.xis.utils.lang.RecordUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ORMapper {

    private final Map<Class<?>, ObjectMapping> mappings = new ConcurrentHashMap<>();
    private final SQLValueConverter valueConverter = new SQLValueConverter();

    void toStatement(@NonNull Object object, @NonNull PreparedStatement statement) throws SQLException {
        ObjectMapping mapping = mapping(object.getClass());
        int index = 1;
        for (ValueMapping value : mapping.values()) {
            statement.setObject(index++, valueConverter.toSqlValue(value.get(object)));
        }
    }

    void toStatement(@NonNull Object object, @NonNull PreparedStatement statement, String... properties) throws SQLException {
        ObjectMapping mapping = mapping(object.getClass());
        int index = 1;
        for (String property : properties) {
            ValueMapping value = mapping.value(property);
            if (value == null) {
                throw new IllegalArgumentException("No simple property " + property + " on " + object.getClass().getName());
            }
            statement.setObject(index++, valueConverter.toSqlValue(value.get(object)));
        }
    }

    private ObjectMapping mapping(Class<?> type) {
        return mappings.computeIfAbsent(type, this::createMapping);
    }

    private ObjectMapping createMapping(Class<?> type) {
        if (type.isRecord()) {
            return createRecordMapping(type);
        }
        var values = new ArrayList<ValueMapping>();
        for (Field field : FieldUtil.getFields(type, field -> isMappedField(type, field))) {
            values.add(new FieldValueMapping(field.getName(), columnName(field), field));
        }
        return new ObjectMapping(values);
    }

    private ObjectMapping createRecordMapping(Class<?> type) {
        var values = new ArrayList<ValueMapping>();
        for (RecordComponent component : type.getRecordComponents()) {
            if (!SQLAnnotationSupport.ignored(component)
                    && (ROMapper.isSimpleType(component.getType()) || SQLAnnotationSupport.jsonColumn(component))) {
                values.add(new RecordValueMapping(component.getName(), columnName(component), component));
            }
        }
        return new ObjectMapping(values);
    }

    private String columnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        return column == null ? field.getName() : column.value();
    }

    private String columnName(RecordComponent component) {
        Column column = component.getAnnotation(Column.class);
        return column == null ? component.getName() : column.value();
    }

    private boolean isMappedField(Class<?> ownerType, Field field) {
        return !Modifier.isStatic(field.getModifiers())
                && !Modifier.isTransient(field.getModifiers())
                && !SQLAnnotationSupport.ignored(ownerType, field)
                && columnMappedType(ownerType, field);
    }

    private boolean columnMappedType(Class<?> ownerType, Field field) {
        if (ROMapper.isSimpleType(field.getType()) || SQLAnnotationSupport.jsonColumn(ownerType, field)) {
            return true;
        }
        return false;
    }

    private record ObjectMapping(List<ValueMapping> values) {

        private ValueMapping value(String name) {
            return values.stream()
                    .filter(value -> value.matches(name))
                    .findFirst()
                    .orElse(null);
        }
    }

    private interface ValueMapping {
        String name();

        String columnName();

        Object get(Object object);

        default boolean matches(String name) {
            return this.name().equals(name) || columnName().equals(name);
        }
    }

    private record FieldValueMapping(String name, String columnName, Field field) implements ValueMapping {

        @Override
        public Object get(Object object) {
            return FieldUtil.getFieldValue(object, field);
        }
    }

    private record RecordValueMapping(String name, String columnName, RecordComponent component) implements ValueMapping {

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
