package one.xis.sql;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
class GenericDeleteByIdMethodHandler implements SQLMethodHandler {
    private final DataSource dataSource;
    private final Class<?> entityType;
    private final SQLValueConverter valueConverter = new SQLValueConverter();
    private EntitySQLMapping mapping;
    private EntitySQLMapping.Property primaryKey;
    private DeleteNode deletePlan;
    private String deleteRootSql;

    @Override
    public boolean matches(Method method) {
        return false;
    }

    @Override
    public void init(Method method) {
        this.mapping = new EntitySQLMapping(entityType);
        try (var connection = dataSource.getConnection()) {
            var graph = new EntityRelationGraph(connection.getMetaData());
            this.primaryKey = graph.primaryKey(mapping);
            this.deletePlan = deleteNode(graph, mapping, primaryKey, new HashSet<>());
        } catch (SQLException e) {
            throw new RuntimeException("Could not read delete metadata for " + mapping.tableName(), e);
        }
        this.deleteRootSql = deleteByPrimaryKeySql(mapping, primaryKey);
    }

    @Override
    public Object invoke(Object[] args) {
        try (var connection = dataSource.getConnection()) {
            int updateCount = deleteDescendants(connection, deletePlan, args[0]);
            updateCount += deleteByPrimaryKey(connection, deleteRootSql, args[0]);
            return updateCount > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Could not execute deleteById for " + mapping.tableName(), e);
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

    private int deleteByPrimaryKey(Connection connection, String sql, Object primaryKeyValue) throws SQLException {
        try (var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, valueConverter.toSqlValue(primaryKeyValue));
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

    private String deleteByPrimaryKeySql(EntitySQLMapping mapping, EntitySQLMapping.Property primaryKey) {
        return "delete from " + mapping.tableName() + " where " + primaryKey.columnName() + " = ?";
    }

    private record DeleteNode(EntitySQLMapping mapping, EntitySQLMapping.Property primaryKey,
                              List<DeleteRelation> children) {
    }

    private record DeleteRelation(EntityRelationGraph.CollectionRelation relation, DeleteNode childNode,
                                  String selectChildPrimaryKeysSql, String deleteChildrenSql) {
    }
}
