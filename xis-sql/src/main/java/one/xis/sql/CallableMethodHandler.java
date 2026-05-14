package one.xis.sql;

import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;

@RequiredArgsConstructor
class CallableMethodHandler implements SQLMethodHandler {
    private final DataSource dataSource;
    private final SQLValueConverter valueConverter = new SQLValueConverter();
    private final ROMapper mapper = new ROMapper();
    private final ModificationReturnMapper returnMapper = new ModificationReturnMapper();
    private Method method;
    private String callSql;
    private CallableKind kind;
    private boolean hasOutParameter;

    @Override
    public boolean matches(Method method) {
        return method.isAnnotationPresent(Function.class) || method.isAnnotationPresent(StoredProcedure.class);
    }

    @Override
    public void init(Method method) {
        this.method = method;
        if (method.isAnnotationPresent(Function.class)) {
            initFunction(method);
            return;
        }
        initStoredProcedure(method);
    }

    @Override
    public Object invoke(Object[] args) {
        ensureInitialized();
        Object[] safeArgs = args == null ? new Object[0] : args;
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareCall(callSql)) {
            registerOutParameter(statement);
            bind(statement, safeArgs);
            if (kind == CallableKind.PROCEDURE && !hasOutParameter) {
                return returnMapper.map(statement.executeUpdate(), safeArgs);
            }
            statement.execute();
            return mapper.toValue(statement.getObject(outParameterIndex()), method.getReturnType());
        } catch (SQLException e) {
            throw new RuntimeException("Could not execute callable method " + method.getName(), e);
        }
    }

    private void initFunction(Method method) {
        if (method.getReturnType() == Void.TYPE || method.getReturnType() == Void.class) {
            throw new IllegalArgumentException("@Function method must return a value: " + method);
        }
        kind = CallableKind.FUNCTION;
        hasOutParameter = true;
        callSql = functionSql(method.getAnnotation(Function.class).value(), method.getParameterCount());
    }

    private void initStoredProcedure(Method method) {
        StoredProcedure annotation = method.getAnnotation(StoredProcedure.class);
        kind = CallableKind.PROCEDURE;
        hasOutParameter = !annotation.out().isBlank();
        if (hasOutParameter) {
            validateProcedureOutParameter(method, annotation.out());
        } else {
            returnMapper.init(method, "@StoredProcedure");
        }
        callSql = procedureSql(annotation.value(), method.getParameterCount(), hasOutParameter);
    }

    private void validateProcedureOutParameter(Method method, String out) {
        if (method.getReturnType() == Void.TYPE || method.getReturnType() == Void.class) {
            throw new IllegalArgumentException("@StoredProcedure with out parameter must return a value: " + method);
        }
        for (var parameter : method.getParameters()) {
            Param param = parameter.getAnnotation(Param.class);
            if (param != null && param.value().equals(out)) {
                throw new IllegalArgumentException("@StoredProcedure out parameter must not duplicate @Param(\""
                        + out + "\"): " + method);
            }
        }
    }

    private void registerOutParameter(CallableStatement statement) throws SQLException {
        if (hasOutParameter) {
            statement.registerOutParameter(outParameterIndex(), sqlType(method.getReturnType()));
        }
    }

    private void bind(CallableStatement statement, Object[] args) throws SQLException {
        int offset = kind == CallableKind.FUNCTION ? 1 : 0;
        for (int i = 0; i < args.length; i++) {
            statement.setObject(i + 1 + offset, valueConverter.toSqlValue(args[i]));
        }
    }

    private int outParameterIndex() {
        return kind == CallableKind.FUNCTION ? 1 : method.getParameterCount() + 1;
    }

    private String functionSql(String name, int parameterCount) {
        return "{ ? = call " + name + "(" + placeholders(parameterCount) + ") }";
    }

    private String procedureSql(String name, int inputParameterCount, boolean outParameter) {
        int parameterCount = inputParameterCount + (outParameter ? 1 : 0);
        return "call " + name + "(" + placeholders(parameterCount) + ")";
    }

    private String placeholders(int count) {
        if (count == 0) {
            return "";
        }
        return String.join(", ", java.util.Collections.nCopies(count, "?"));
    }

    private int sqlType(Class<?> type) {
        if (type == String.class || type == Character.class || type == Character.TYPE || type == char[].class
                || type.isEnum()) {
            return Types.VARCHAR;
        }
        if (type == Integer.class || type == Integer.TYPE) return Types.INTEGER;
        if (type == Long.class || type == Long.TYPE) return Types.BIGINT;
        if (type == Boolean.class || type == Boolean.TYPE) return Types.BOOLEAN;
        if (type == BigDecimal.class) return Types.DECIMAL;
        if (type == Double.class || type == Double.TYPE) return Types.DOUBLE;
        if (type == Float.class || type == Float.TYPE) return Types.REAL;
        if (type == Short.class || type == Short.TYPE) return Types.SMALLINT;
        if (type == Byte.class || type == Byte.TYPE) return Types.TINYINT;
        if (type == java.sql.Date.class || type == java.time.LocalDate.class) return Types.DATE;
        if (type == java.sql.Time.class || type == java.time.LocalTime.class) return Types.TIME;
        if (type == java.sql.Timestamp.class || type == java.time.LocalDateTime.class
                || type == java.time.Instant.class) {
            return Types.TIMESTAMP;
        }
        return Types.JAVA_OBJECT;
    }

    private void ensureInitialized() {
        if (method == null) {
            throw new IllegalStateException("CallableMethodHandler was not initialized");
        }
    }

    private enum CallableKind {
        FUNCTION,
        PROCEDURE
    }
}
