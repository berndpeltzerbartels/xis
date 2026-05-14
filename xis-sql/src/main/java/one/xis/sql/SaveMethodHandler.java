package one.xis.sql;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

@RequiredArgsConstructor
class SaveMethodHandler implements SQLMethodHandler {
    private final DataSource dataSource;
    private final Class<?> explicitEntityType;
    private final SQLValueConverter valueConverter = new SQLValueConverter();
    private final SQLMethodSupport methodSupport = new SQLMethodSupport();
    private final ModificationReturnMapper returnMapper = new ModificationReturnMapper();
    private Method method;
    private boolean sqlSave;
    private String sql;
    private List<SQLMethodSupport.BindParameter> bindParameterIndexes;
    private EntitySQLMapping mapping;
    private List<EntitySQLMapping.Property> primaryKeys;
    private List<EntitySQLMapping.Property> nonPrimaryKeys;
    private List<EntitySQLMapping.Property> saveProperties;
    private List<CollectionSavePlan> collectionSavePlans;
    private String existsSql;
    private String insertSql;
    private String updateSql;

    SaveMethodHandler(DataSource dataSource) {
        this(dataSource, null);
    }

    @Override
    public boolean matches(Method method) {
        return method.isAnnotationPresent(Save.class);
    }

    @Override
    public void init(Method method) {
        this.method = method;
        Save save = method.getAnnotation(Save.class);
        if (save != null && !save.value().isBlank()) {
            initSqlSave(method);
            return;
        }
        validateSignature(method);
        this.mapping = new EntitySQLMapping(entityType(method));
        try (var connection = dataSource.getConnection()) {
            var graph = new EntityRelationGraph(connection.getMetaData());
            this.primaryKeys = graph.primaryKeys(mapping);
            this.saveProperties = graph.saveProperties(mapping);
            this.collectionSavePlans = primaryKeys.size() == 1
                    ? graph.collectionRelations(mapping, primaryKeys.get(0)).stream()
                    .map(CollectionSavePlan::new)
                    .toList()
                    : List.of();
        } catch (SQLException e) {
            throw new RuntimeException("Could not read save metadata for " + mapping.tableName(), e);
        }
        this.nonPrimaryKeys = saveProperties.stream()
                .filter(property -> !primaryKeys.contains(property))
                .toList();
        this.existsSql = existsSql(mapping, primaryKeys);
        this.insertSql = insertSql(mapping, saveProperties);
        this.updateSql = updateSql(mapping, primaryKeys, nonPrimaryKeys);
        returnMapper.init(method, "@Save");
    }

    @Override
    public Object invoke(Object[] args) {
        ensureInitialized();
        Object[] safeArgs = args == null ? new Object[0] : args;
        if (sqlSave) {
            return invokeSqlSave(safeArgs);
        }
        Object entity = safeArgs[0];
        try (var connection = dataSource.getConnection()) {
            int updateCount = exists(connection, entity, existsSql) ? update(connection, entity) : insert(connection, entity);
            updateCount += saveCollections(connection, entity);
            return returnMapper.map(updateCount, safeArgs);
        } catch (SQLException e) {
            throw new RuntimeException("Could not execute save method " + method.getName(), e);
        }
    }

    private void validateSignature(Method method) {
        if (method.getParameterCount() != 1) {
            throw new IllegalArgumentException("@Save method must have exactly one entity parameter: " + method);
        }
    }

    private Class<?> entityType(Method method) {
        return explicitEntityType == null ? method.getParameterTypes()[0] : explicitEntityType;
    }

    private void initSqlSave(Method method) {
        this.sqlSave = true;
        returnMapper.init(method, "@Save");
        SQLMethodSupport.ParsedSql parsedSql = methodSupport.parseSql(method.getAnnotation(Save.class).value(),
                method, "@Save", returnMapper.returnedParameterIndexes());
        this.sql = parsedSql.sql();
        this.bindParameterIndexes = parsedSql.bindParameterIndexes();
    }

    private Object invokeSqlSave(Object[] args) {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            bind(statement, args);
            return returnMapper.map(statement.executeUpdate(), args);
        } catch (SQLException e) {
            throw new RuntimeException("Could not execute save method " + method.getName(), e);
        }
    }

    private void bind(java.sql.PreparedStatement statement, Object[] args) throws SQLException {
        for (int i = 0; i < bindParameterIndexes.size(); i++) {
            SQLMethodSupport.BindParameter bindParameter = bindParameterIndexes.get(i);
            Object value = args[bindParameter.parameterIndex()];
            if (bindParameter.propertyName() != null) {
                value = property(new EntitySQLMapping(value.getClass()), bindParameter.propertyName()).get(value);
            }
            statement.setObject(i + 1, valueConverter.toSqlValue(value));
        }
    }

    private EntitySQLMapping.Property property(EntitySQLMapping mapping, String name) {
        EntitySQLMapping.Property property = mapping.propertyByNameOrColumn(name);
        if (property == null) {
            throw new IllegalArgumentException("No simple property " + name + " on " + mapping.type().getName());
        }
        return property;
    }

    private void ensureInitialized() {
        if (method == null) {
            throw new IllegalStateException("SaveMethodHandler was not initialized");
        }
    }

    private boolean exists(Connection connection, Object entity, String sql) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            bindProperties(statement, entity, primaryKeys, 1);
            try (var resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean exists(Connection connection, Object entity, EntitySQLMapping.Property primaryKey,
                           String sql) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, valueConverter.toSqlValue(primaryKey.get(entity)));
            try (var resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private int insert(Connection connection, Object entity) throws SQLException {
        try (var statement = connection.prepareStatement(insertSql)) {
            int index = 1;
            for (EntitySQLMapping.Property property : saveProperties) {
                statement.setObject(index++, valueConverter.toSqlValue(property.get(entity)));
            }
            return statement.executeUpdate();
        }
    }

    private int update(Connection connection, Object entity) throws SQLException {
        try (var statement = connection.prepareStatement(updateSql)) {
            int index = 1;
            for (EntitySQLMapping.Property property : nonPrimaryKeys) {
                statement.setObject(index++, valueConverter.toSqlValue(property.get(entity)));
            }
            bindProperties(statement, entity, primaryKeys, index);
            return statement.executeUpdate();
        }
    }

    private int bindProperties(java.sql.PreparedStatement statement, Object entity,
                               List<EntitySQLMapping.Property> properties, int startIndex) throws SQLException {
        int index = startIndex;
        for (EntitySQLMapping.Property property : properties) {
            statement.setObject(index++, valueConverter.toSqlValue(property.get(entity)));
        }
        return index;
    }

    private int saveCollections(Connection connection, Object parent) throws SQLException {
        int updateCount = 0;
        for (CollectionSavePlan plan : collectionSavePlans) {
            updateCount += saveCollection(connection, parent, plan);
        }
        return updateCount;
    }

    private int saveCollection(Connection connection, Object parent, CollectionSavePlan plan) throws SQLException {
        Collection<?> children = plan.relation().accessor().get(parent);
        if (children == null || children.isEmpty()) {
            return 0;
        }
        Object parentPrimaryKeyValue = plan.relation().parentPrimaryKey().get(parent);
        int updateCount = 0;
        for (Object child : children) {
            if (child != null) {
                updateCount += exists(connection, child, plan.relation().childPrimaryKey(), plan.existsSql())
                        ? updateChild(connection, child, parentPrimaryKeyValue, plan)
                        : insertChild(connection, child, parentPrimaryKeyValue, plan);
            }
        }
        return updateCount;
    }

    private int insertChild(Connection connection, Object child, Object parentPrimaryKeyValue,
                            CollectionSavePlan plan) throws SQLException {
        try (var statement = connection.prepareStatement(plan.insertSql())) {
            int index = 1;
            for (EntitySQLMapping.Property property : plan.relation().childProperties()) {
                statement.setObject(index++, valueConverter.toSqlValue(property.get(child)));
            }
            statement.setObject(index, valueConverter.toSqlValue(parentPrimaryKeyValue));
            return statement.executeUpdate();
        }
    }

    private int updateChild(Connection connection, Object child, Object parentPrimaryKeyValue,
                            CollectionSavePlan plan) throws SQLException {
        try (var statement = connection.prepareStatement(plan.updateSql())) {
            int index = 1;
            for (EntitySQLMapping.Property property : plan.relation().nonPrimaryKeyChildProperties()) {
                statement.setObject(index++, valueConverter.toSqlValue(property.get(child)));
            }
            statement.setObject(index++, valueConverter.toSqlValue(parentPrimaryKeyValue));
            statement.setObject(index, valueConverter.toSqlValue(plan.relation().childPrimaryKey().get(child)));
            return statement.executeUpdate();
        }
    }

    private String existsSql(EntitySQLMapping mapping, List<EntitySQLMapping.Property> primaryKeys) {
        return "select 1 from " + mapping.tableName() + " where " + whereClause(primaryKeys);
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

    private String updateSql(EntitySQLMapping mapping, List<EntitySQLMapping.Property> primaryKeys,
                             List<EntitySQLMapping.Property> nonPrimaryKeys) {
        if (nonPrimaryKeys.isEmpty()) {
            throw new IllegalStateException("@Save needs at least one non-primary-key property for " + mapping.tableName());
        }
        var setClause = new StringJoiner(", ");
        for (EntitySQLMapping.Property property : nonPrimaryKeys) {
            setClause.add(property.columnName() + " = ?");
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

    private record CollectionSavePlan(EntityRelationGraph.CollectionRelation relation,
                                      String existsSql, String insertSql, String updateSql) {

        private CollectionSavePlan(EntityRelationGraph.CollectionRelation relation) {
            this(relation, existsSql(relation), insertSql(relation), updateSql(relation));
        }

        private static String existsSql(EntityRelationGraph.CollectionRelation relation) {
            return "select 1 from " + relation.childMapping().tableName()
                    + " where " + relation.childPrimaryKey().columnName() + " = ?";
        }

        private static String insertSql(EntityRelationGraph.CollectionRelation relation) {
            var columns = new StringJoiner(", ");
            var placeholders = new StringJoiner(", ");
            for (EntitySQLMapping.Property property : relation.childProperties()) {
                columns.add(property.columnName());
                placeholders.add("?");
            }
            columns.add(relation.foreignKeyColumn());
            placeholders.add("?");
            return "insert into " + relation.childMapping().tableName()
                    + " (" + columns + ") values (" + placeholders + ")";
        }

        private static String updateSql(EntityRelationGraph.CollectionRelation relation) {
            var setClause = new StringJoiner(", ");
            for (EntitySQLMapping.Property property : relation.nonPrimaryKeyChildProperties()) {
                setClause.add(property.columnName() + " = ?");
            }
            setClause.add(relation.foreignKeyColumn() + " = ?");
            return "update " + relation.childMapping().tableName()
                    + " set " + setClause + " where " + relation.childPrimaryKey().columnName() + " = ?";
        }
    }
}
