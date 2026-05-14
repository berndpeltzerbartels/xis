package one.xis.sql;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
class SelectMethodHandler implements SQLMethodHandler {
    private final DataSource dataSource;
    private final ROMapper mapper;
    private final SQLValueConverter valueConverter = new SQLValueConverter();
    private final SQLMethodSupport methodSupport = new SQLMethodSupport();
    private Method method;
    private String sql;
    private List<SQLMethodSupport.BindParameter> bindParameterIndexes;
    private Class<?> resultType;
    private ResultCardinality cardinality;

    @Override
    public boolean matches(Method method) {
        return method.isAnnotationPresent(Select.class);
    }

    @Override
    public void init(Method method) {
        this.method = method;
        SQLMethodSupport.ParsedSql parsedSql = methodSupport.parseSql(method.getAnnotation(Select.class).value(), method, "@Select");
        this.sql = parsedSql.sql();
        this.bindParameterIndexes = parsedSql.bindParameterIndexes();
        this.cardinality = cardinality(method);
        this.resultType = resultType(method, cardinality);
    }

    @Override
    public Object invoke(Object[] args) {
        ensureInitialized();
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(sql)) {
            bind(statement, args == null ? new Object[0] : args);
            try (var resultSet = statement.executeQuery()) {
                return map(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not execute select method " + method.getName(), e);
        }
    }

    private void ensureInitialized() {
        if (method == null) {
            throw new IllegalStateException("SelectMethodHandler was not initialized");
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

    private Object map(ResultSet resultSet) throws SQLException {
        return switch (cardinality) {
            case LIST -> mapList(resultSet);
            case OPTIONAL -> Optional.ofNullable(mapSingle(resultSet));
            case SINGLE -> mapSingle(resultSet);
        };
    }

    private Object mapList(ResultSet resultSet) throws SQLException {
        if (ROMapper.isSimpleType(resultType)) {
            return mapScalarList(resultSet);
        }
        return mapper.toObjects(resultSet, resultType);
    }

    private List<Object> mapScalarList(ResultSet resultSet) throws SQLException {
        var values = new java.util.ArrayList<>();
        while (resultSet.next()) {
            values.add(readScalar(resultSet));
        }
        return values;
    }

    private Object mapSingle(ResultSet resultSet) throws SQLException {
        if (ROMapper.isSimpleType(resultType)) {
            return resultSet.next() ? readScalar(resultSet) : null;
        }
        return mapper.toObject(resultSet, resultType);
    }

    private Object readScalar(ResultSet resultSet) throws SQLException {
        Object value = resultSet.getObject(1);
        if (resultSet.wasNull()) {
            return null;
        }
        return mapper.toValue(value, resultType);
    }

    private ResultCardinality cardinality(Method method) {
        Class<?> returnType = method.getReturnType();
        if (returnType == Void.TYPE || returnType == Void.class) {
            throw new IllegalArgumentException("@Select method must return a value: " + method);
        }
        if (Optional.class.isAssignableFrom(returnType)) {
            return ResultCardinality.OPTIONAL;
        }
        if (Collection.class.isAssignableFrom(returnType)) {
            return ResultCardinality.LIST;
        }
        return ResultCardinality.SINGLE;
    }

    private Class<?> resultType(Method method, ResultCardinality cardinality) {
        if (cardinality == ResultCardinality.SINGLE) {
            return method.getReturnType();
        }
        return methodSupport.genericReturnType(method, "@Select");
    }

    private enum ResultCardinality {
        SINGLE, OPTIONAL, LIST
    }
}
