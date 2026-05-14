package one.xis.sql;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@RequiredArgsConstructor
class UpdateMethodHandler implements SQLMethodHandler {
    private final DataSource dataSource;
    private final SQLValueConverter valueConverter = new SQLValueConverter();
    private final SQLMethodSupport methodSupport = new SQLMethodSupport();
    private final ModificationReturnMapper returnMapper = new ModificationReturnMapper();
    private Method method;
    private String sql;
    private List<SQLMethodSupport.BindParameter> bindParameterIndexes;
    private EntitySQLMapping mapping;
    private List<EntitySQLMapping.Property> autoBindProperties;

    @Override
    public boolean matches(Method method) {
        return method.isAnnotationPresent(Update.class) || method.isAnnotationPresent(Insert.class);
    }

    @Override
    public void init(Method method) {
        this.method = method;
        String annotationName = annotationName(method);
        returnMapper.init(method, annotationName);
        if (sql(method).isBlank()) {
            initEntityModification(method, annotationName);
        } else {
            SQLMethodSupport.ParsedSql parsedSql = methodSupport.parseSql(sql(method), method, annotationName,
                    returnMapper.returnedParameterIndexes());
            this.sql = parsedSql.sql();
            this.bindParameterIndexes = parsedSql.bindParameterIndexes();
        }
    }

    @Override
    public Object invoke(Object[] args) {
        ensureInitialized();
        Object[] safeArgs = args == null ? new Object[0] : args;
        try (var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(sql)) {
            bind(statement, safeArgs);
            return returnMapper.map(statement.executeUpdate(), safeArgs);
        } catch (SQLException e) {
            throw new RuntimeException("Could not execute modification method " + method.getName(), e);
        }
    }

    private void ensureInitialized() {
        if (method == null) {
            throw new IllegalStateException("UpdateMethodHandler was not initialized");
        }
    }

    private void bind(PreparedStatement statement, Object[] args) throws SQLException {
        for (int i = 0; i < bindParameterIndexes.size(); i++) {
            SQLMethodSupport.BindParameter bindParameter = bindParameterIndexes.get(i);
            Object value = args[bindParameter.parameterIndex()];
            if (bindParameter.propertyName() != null) {
                value = property(mapping(value.getClass()), bindParameter.propertyName()).get(value);
            }
            statement.setObject(i + 1, valueConverter.toSqlValue(value));
        }
    }

    private void initEntityModification(Method method, String annotationName) {
        if (method.getParameterCount() != 1) {
            throw new IllegalArgumentException(annotationName + " without SQL must have exactly one entity parameter: " + method);
        }
        Class<?> entityType = method.getParameterTypes()[0];
        this.mapping = mapping(entityType);
        try (var connection = dataSource.getConnection()) {
            var graph = new EntityRelationGraph(connection.getMetaData());
            var primaryKeys = graph.primaryKeys(mapping);
            var persistedProperties = graph.saveProperties(mapping);
            if (method.isAnnotationPresent(Insert.class)) {
                this.autoBindProperties = persistedProperties;
                this.sql = insertSql(mapping, autoBindProperties);
            } else {
                this.autoBindProperties = updateBindProperties(persistedProperties, primaryKeys);
                this.sql = updateSql(mapping, persistedProperties, primaryKeys);
            }
            this.bindParameterIndexes = autoBindProperties.stream()
                    .map(property -> new SQLMethodSupport.BindParameter(0, property.name()))
                    .toList();
        } catch (SQLException e) {
            throw new RuntimeException("Could not read metadata for " + mapping.tableName(), e);
        }
    }

    private EntitySQLMapping mapping(Class<?> type) {
        return new EntitySQLMapping(type);
    }

    private EntitySQLMapping.Property property(EntitySQLMapping mapping, String name) {
        EntitySQLMapping.Property property = mapping.propertyByNameOrColumn(name);
        if (property == null) {
            throw new IllegalArgumentException("No simple property " + name + " on " + mapping.type().getName());
        }
        return property;
    }

    private List<EntitySQLMapping.Property> updateBindProperties(List<EntitySQLMapping.Property> persistedProperties,
                                                                 List<EntitySQLMapping.Property> primaryKeys) {
        var properties = new ArrayList<EntitySQLMapping.Property>();
        properties.addAll(persistedProperties.stream()
                .filter(property -> !primaryKeys.contains(property))
                .toList());
        properties.addAll(primaryKeys);
        return List.copyOf(properties);
    }

    private String insertSql(EntitySQLMapping mapping, List<EntitySQLMapping.Property> properties) {
        var columns = new StringJoiner(", ");
        var placeholders = new StringJoiner(", ");
        for (EntitySQLMapping.Property property : properties) {
            columns.add(property.columnName());
            placeholders.add("?");
        }
        return "insert into " + mapping.tableName() + " (" + columns + ") values (" + placeholders + ")";
    }

    private String updateSql(EntitySQLMapping mapping, List<EntitySQLMapping.Property> persistedProperties,
                             List<EntitySQLMapping.Property> primaryKeys) {
        var setClause = new StringJoiner(", ");
        persistedProperties.stream()
                .filter(property -> !primaryKeys.contains(property))
                .forEach(property -> setClause.add(property.columnName() + " = ?"));
        if (setClause.length() == 0) {
            throw new IllegalStateException("@Update needs at least one non-primary-key property for " + mapping.tableName());
        }
        return "update " + mapping.tableName() + " set " + setClause + " where " + whereClause(primaryKeys);
    }

    private String whereClause(List<EntitySQLMapping.Property> primaryKeys) {
        var where = new StringJoiner(" and ");
        for (EntitySQLMapping.Property primaryKey : primaryKeys) {
            where.add(primaryKey.columnName() + " = ?");
        }
        return where.toString();
    }

    private String sql(Method method) {
        Update update = method.getAnnotation(Update.class);
        if (update != null) {
            return update.value();
        }
        return method.getAnnotation(Insert.class).value();
    }

    private String annotationName(Method method) {
        return method.isAnnotationPresent(Update.class) ? "@Update" : "@Insert";
    }
}
