package one.xis.sql;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class GeneratedPrimaryKey {

    boolean isAutoIncrement(Connection connection, EntitySQLMapping mapping,
                            EntitySQLMapping.Property primaryKey) throws SQLException {
        if (!primaryKey.writable()) {
            return false;
        }
        try (ResultSet columns = connection.getMetaData().getColumns(null, null,
                mapping.tableName(), primaryKey.columnName())) {
            if (columns.next()) {
                return "YES".equalsIgnoreCase(columns.getString("IS_AUTOINCREMENT"));
            }
        }
        try (ResultSet columns = connection.getMetaData().getColumns(null, null,
                mapping.tableName().toUpperCase(Locale.ROOT), primaryKey.columnName().toUpperCase(Locale.ROOT))) {
            return columns.next() && "YES".equalsIgnoreCase(columns.getString("IS_AUTOINCREMENT"));
        }
    }

    boolean isUnset(EntitySQLMapping.Property primaryKey, Object entity) {
        Object value = primaryKey.get(entity);
        if (value == null) {
            return true;
        }
        if (value instanceof Number number) {
            return number.doubleValue() == 0d;
        }
        return false;
    }

    List<EntitySQLMapping.Property> unsetGeneratedKeys(List<EntitySQLMapping.Property> generatedKeys, Object entity) {
        var unset = new ArrayList<EntitySQLMapping.Property>();
        for (EntitySQLMapping.Property generatedKey : generatedKeys) {
            if (isUnset(generatedKey, entity)) {
                unset.add(generatedKey);
            }
        }
        return List.copyOf(unset);
    }

    String[] columnNames(List<EntitySQLMapping.Property> generatedKeys) {
        return generatedKeys.stream()
                .map(EntitySQLMapping.Property::columnName)
                .toArray(String[]::new);
    }

    void applyGeneratedKeys(ResultSet generatedKeys, List<EntitySQLMapping.Property> primaryKeys,
                            Object entity) throws SQLException {
        if (generatedKeys.next()) {
            for (int i = 0; i < primaryKeys.size(); i++) {
                var primaryKey = primaryKeys.get(i);
                primaryKey.set(entity, toPropertyValue(generatedKeys.getObject(i + 1), primaryKey.type()));
            }
        }
    }

    private Object toPropertyValue(Object value, Class<?> targetType) {
        if (value == null || targetType.isInstance(value)) {
            return value;
        }
        if (targetType == Long.TYPE || targetType == Long.class) {
            return ((Number) value).longValue();
        }
        if (targetType == Integer.TYPE || targetType == Integer.class) {
            return ((Number) value).intValue();
        }
        if (targetType == Short.TYPE || targetType == Short.class) {
            return ((Number) value).shortValue();
        }
        if (targetType == Byte.TYPE || targetType == Byte.class) {
            return ((Number) value).byteValue();
        }
        if (targetType == BigInteger.class) {
            return BigInteger.valueOf(((Number) value).longValue());
        }
        if (targetType == String.class) {
            return value.toString();
        }
        return value;
    }
}
