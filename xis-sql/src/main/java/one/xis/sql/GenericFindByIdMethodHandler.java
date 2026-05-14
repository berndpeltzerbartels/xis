package one.xis.sql;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.Optional;

@RequiredArgsConstructor
class GenericFindByIdMethodHandler implements SQLMethodHandler {
    private final DataSource dataSource;
    private final Class<?> entityType;
    private final SQLValueConverter valueConverter = new SQLValueConverter();
    private EntitySQLMapping mapping;
    private EntitySQLMapping.Property primaryKey;
    private String sql;

    @Override
    public boolean matches(Method method) {
        return false;
    }

    @Override
    public void init(Method method) {
        this.mapping = new EntitySQLMapping(entityType);
        try (var connection = dataSource.getConnection()) {
            this.primaryKey = new EntityRelationGraph(connection.getMetaData()).primaryKey(mapping);
        } catch (SQLException e) {
            throw new RuntimeException("Could not read primary-key metadata for " + mapping.tableName(), e);
        }
        this.sql = "select * from " + mapping.tableName() + " where " + primaryKey.columnName() + " = ?";
    }

    @Override
    public Object invoke(Object[] args) {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setObject(1, valueConverter.toSqlValue(args[0]));
            try (var resultSet = statement.executeQuery()) {
                return Optional.ofNullable(new ROMapper().toObject(resultSet, entityType));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not execute findById for " + mapping.tableName(), e);
        }
    }
}
