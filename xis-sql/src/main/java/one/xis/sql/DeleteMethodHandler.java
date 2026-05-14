package one.xis.sql;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

@RequiredArgsConstructor
class DeleteMethodHandler implements SQLMethodHandler {
    private final DataSource dataSource;
    private final Class<?> explicitEntityType;
    private final SQLValueConverter valueConverter = new SQLValueConverter();
    private final SQLMethodSupport methodSupport = new SQLMethodSupport();
    private final ModificationReturnMapper returnMapper = new ModificationReturnMapper();
    private Method method;
    private boolean sqlDelete;
    private String sql;
    private List<SQLMethodSupport.BindParameter> bindParameterIndexes;
    private EntitySQLMapping mapping;
    private List<EntitySQLMapping.Property> primaryKeys;
    private DeleteNode deletePlan;
    private String deleteRootSql;

    DeleteMethodHandler(DataSource dataSource) {
        this(dataSource, null);
    }

    @Override
    public boolean matches(Method method) {
        return method.isAnnotationPresent(Delete.class);
    }

    @Override
    public void init(Method method) {
        this.method = method;
        if (isSqlDelete(method)) {
            initSqlDelete(method);
            return;
        }
        validateEntitySignature(method);
        this.mapping = new EntitySQLMapping(entityType(method));
        try (var connection = dataSource.getConnection()) {
            var graph = new EntityRelationGraph(connection.getMetaData());
            this.primaryKeys = graph.primaryKeys(mapping);
            this.deletePlan = primaryKeys.size() == 1
                    ? deleteNode(graph, mapping, primaryKeys.get(0), new HashSet<>())
                    : new DeleteNode(mapping, null, List.of());
        } catch (SQLException e) {
            throw new RuntimeException("Could not read delete metadata for " + mapping.tableName(), e);
        }
        this.deleteRootSql = deleteByPrimaryKeySql(mapping, primaryKeys);
        returnMapper.init(method, "@Delete");
    }

    @Override
    public Object invoke(Object[] args) {
        ensureInitialized();
        Object[] safeArgs = args == null ? new Object[0] : args;
        if (sqlDelete) {
            return invokeSqlDelete(safeArgs);
        }
        Object entity = safeArgs[0];
        try (var connection = dataSource.getConnection()) {
            int updateCount = primaryKeys.size() == 1
                    ? deleteDescendants(connection, deletePlan, primaryKeys.get(0).get(entity))
                    : 0;
            updateCount += deleteByPrimaryKey(connection, deleteRootSql, entity);
            return returnMapper.map(updateCount, safeArgs);
        } catch (SQLException e) {
            throw new RuntimeException("Could not execute delete method " + method.getName(), e);
        }
    }

    private void initSqlDelete(Method method) {
        this.sqlDelete = true;
        returnMapper.init(method, "@Delete");
        SQLMethodSupport.ParsedSql parsedSql = methodSupport.parseSql(method.getAnnotation(Delete.class).value(),
                method, "@Delete", returnMapper.returnedParameterIndexes());
        this.sql = parsedSql.sql();
        this.bindParameterIndexes = parsedSql.bindParameterIndexes();
    }

    private Object invokeSqlDelete(Object[] args) {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            bind(statement, args);
            return returnMapper.map(statement.executeUpdate(), args);
        } catch (SQLException e) {
            throw new RuntimeException("Could not execute delete method " + method.getName(), e);
        }
    }

    private void bind(PreparedStatement statement, Object[] args) throws SQLException {
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

    private boolean isSqlDelete(Method method) {
        Delete delete = method.getAnnotation(Delete.class);
        return delete != null && !delete.value().isBlank();
    }

    private void validateEntitySignature(Method method) {
        if (method.getParameterCount() != 1) {
            throw new IllegalArgumentException("@Delete method must have exactly one entity parameter: " + method);
        }
    }

    private Class<?> entityType(Method method) {
        return explicitEntityType == null ? method.getParameterTypes()[0] : explicitEntityType;
    }

    private void ensureInitialized() {
        if (method == null) {
            throw new IllegalStateException("DeleteMethodHandler was not initialized");
        }
    }

    private DeleteNode deleteNode(EntityRelationGraph graph, EntitySQLMapping mapping,
                                  EntitySQLMapping.Property primaryKey, Set<Class<?>> path) throws SQLException {
        if (!path.add(mapping.type())) {
            throw new IllegalStateException("Cyclic delete relations are not supported for " + mapping.type().getName());
        }
        var children = new ArrayList<DeleteRelation>();
        for (EntityRelationGraph.CollectionRelation relation : graph.collectionRelations(mapping, primaryKey)) {
            DeleteNode childNode = deleteNode(graph, relation.childMapping(), relation.childPrimaryKey(), new HashSet<>(path));
            children.add(new DeleteRelation(relation, childNode, selectChildPrimaryKeysSql(relation), deleteChildrenSql(relation)));
        }
        return new DeleteNode(mapping, primaryKey, List.copyOf(children));
    }

    private int deleteDescendants(Connection connection, DeleteNode node, Object primaryKeyValue) throws SQLException {
        int updateCount = 0;
        for (DeleteRelation relation : node.children()) {
            List<Object> childPrimaryKeys = selectChildPrimaryKeys(connection, relation, primaryKeyValue);
            for (Object childPrimaryKey : childPrimaryKeys) {
                updateCount += deleteDescendants(connection, relation.childNode(), childPrimaryKey);
            }
            if (!relation.relation().deleteCascades()) {
                updateCount += deleteByForeignKey(connection, relation.deleteChildrenSql(), primaryKeyValue);
            }
        }
        return updateCount;
    }

    private List<Object> selectChildPrimaryKeys(Connection connection, DeleteRelation relation,
                                                Object primaryKeyValue) throws SQLException {
        var primaryKeys = new ArrayList<>();
        try (var statement = connection.prepareStatement(relation.selectChildPrimaryKeysSql())) {
            statement.setObject(1, valueConverter.toSqlValue(primaryKeyValue));
            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    primaryKeys.add(resultSet.getObject(1));
                }
            }
        }
        return primaryKeys;
    }

    private int deleteByForeignKey(Connection connection, String sql, Object primaryKeyValue) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, valueConverter.toSqlValue(primaryKeyValue));
            return statement.executeUpdate();
        }
    }

    private int deleteByPrimaryKey(Connection connection, String sql, Object entity) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            int index = 1;
            for (EntitySQLMapping.Property primaryKey : primaryKeys) {
                statement.setObject(index++, valueConverter.toSqlValue(primaryKey.get(entity)));
            }
            return statement.executeUpdate();
        }
    }

    private String selectChildPrimaryKeysSql(EntityRelationGraph.CollectionRelation relation) {
        return "select " + relation.childPrimaryKey().columnName()
                + " from " + relation.childMapping().tableName()
                + " where " + relation.foreignKeyColumn() + " = ?";
    }

    private String deleteChildrenSql(EntityRelationGraph.CollectionRelation relation) {
        return "delete from " + relation.childMapping().tableName()
                + " where " + relation.foreignKeyColumn() + " = ?";
    }

    private String deleteByPrimaryKeySql(EntitySQLMapping mapping, List<EntitySQLMapping.Property> primaryKeys) {
        return "delete from " + mapping.tableName() + " where " + whereClause(primaryKeys);
    }

    private String whereClause(List<EntitySQLMapping.Property> primaryKeys) {
        var where = new StringJoiner(" and ");
        for (EntitySQLMapping.Property primaryKey : primaryKeys) {
            where.add(primaryKey.columnName() + " = ?");
        }
        return where.toString();
    }

    private record DeleteNode(EntitySQLMapping mapping, EntitySQLMapping.Property primaryKey,
                              List<DeleteRelation> children) {
    }

    private record DeleteRelation(EntityRelationGraph.CollectionRelation relation, DeleteNode childNode,
                                  String selectChildPrimaryKeysSql, String deleteChildrenSql) {
    }
}
