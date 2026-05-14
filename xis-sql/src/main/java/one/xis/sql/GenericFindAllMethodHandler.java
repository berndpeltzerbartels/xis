package one.xis.sql;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.SQLException;

@RequiredArgsConstructor
class GenericFindAllMethodHandler implements SQLMethodHandler {
    private final DataSource dataSource;
    private final Class<?> entityType;
    private EntitySQLMapping mapping;
    private String sql;

    @Override
    public boolean matches(Method method) {
        return false;
    }

    @Override
    public void init(Method method) {
        this.mapping = new EntitySQLMapping(entityType);
        this.sql = "select * from " + mapping.tableName();
    }

    @Override
    public Object invoke(Object[] args) {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql);
             var resultSet = statement.executeQuery()) {
            return new ROMapper().toObjects(resultSet, entityType);
        } catch (SQLException e) {
            throw new RuntimeException("Could not execute findAll for " + mapping.tableName(), e);
        }
    }
}
