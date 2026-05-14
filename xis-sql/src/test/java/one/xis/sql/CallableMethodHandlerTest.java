package one.xis.sql;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CallableMethodHandlerTest {

    private JdbcDataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:callable-method-handler;DB_CLOSE_DELAY=-1");
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("drop alias if exists DOUBLE_VALUE");
            statement.execute("drop alias if exists GREETING");
            statement.execute("create alias DOUBLE_VALUE for \"" + CallableMethods.class.getName() + ".doubleValue\"");
            statement.execute("create alias GREETING for \"" + CallableMethods.class.getName() + ".greeting\"");
        }
    }

    @Test
    void callsFunction() throws ReflectiveOperationException {
        int result = invoke("doubleValue", new Class[]{int.class}, 21);

        assertEquals(42, result);
    }

    @Test
    void callsFunctionWithStringReturn() throws ReflectiveOperationException {
        String result = invoke("greeting", new Class[]{String.class}, "Ada");

        assertEquals("Hello Ada", result);
    }

    @Test
    void callsStoredProcedureWithOneOutParameter() throws ReflectiveOperationException {
        var dataSource = new FakeCallableDataSource(42);
        Method method = CallableRepository.class.getDeclaredMethod("addFive", int.class);
        var handler = new CallableMethodHandler(dataSource);

        handler.init(method);
        int result = (int) handler.invoke(new Object[]{37});

        assertEquals(42, result);
        assertEquals("call ADD_FIVE(?, ?)", dataSource.sql);
        assertEquals(List.of("registerOutParameter(2," + Types.INTEGER + ")", "setObject(1,37)", "execute()", "getObject(2)"),
                dataSource.calls);
    }

    @Test
    void callsStoredProcedureWithoutOutParameterAndReturnsUpdateCount() throws ReflectiveOperationException {
        var dataSource = new FakeCallableDataSource(3);
        Method method = CallableRepository.class.getDeclaredMethod("archive", long.class);
        var handler = new CallableMethodHandler(dataSource);

        handler.init(method);
        int result = (int) handler.invoke(new Object[]{17L});

        assertEquals(3, result);
        assertEquals("call ARCHIVE_CUSTOMER(?)", dataSource.sql);
        assertEquals(List.of("setObject(1,17)", "executeUpdate()"), dataSource.calls);
    }

    @Test
    void rejectsVoidFunction() throws ReflectiveOperationException {
        Method method = CallableRepository.class.getDeclaredMethod("invalidFunction");
        var handler = new CallableMethodHandler(dataSource);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> handler.init(method));

        assertEquals("@Function method must return a value: " + method, exception.getMessage());
    }

    @Test
    void rejectsDuplicateOutParameterName() throws ReflectiveOperationException {
        Method method = CallableRepository.class.getDeclaredMethod("duplicateOut", int.class);
        var handler = new CallableMethodHandler(dataSource);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> handler.init(method));

        assertEquals("@StoredProcedure out parameter must not duplicate @Param(\"result\"): " + method, exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    private <T> T invoke(String methodName, Class<?>[] parameterTypes, Object... args) throws ReflectiveOperationException {
        Method method = CallableRepository.class.getDeclaredMethod(methodName, parameterTypes);
        var handler = new CallableMethodHandler(dataSource);
        handler.init(method);
        return (T) handler.invoke(args);
    }

    interface CallableRepository {

        @Function("DOUBLE_VALUE")
        int doubleValue(int value);

        @Function("GREETING")
        String greeting(String name);

        @StoredProcedure(value = "ADD_FIVE", out = "result")
        int addFive(@Param("value") int value);

        @StoredProcedure("ARCHIVE_CUSTOMER")
        int archive(@Param("id") long id);

        @Function("DOUBLE_VALUE")
        void invalidFunction();

        @StoredProcedure(value = "ADD_FIVE", out = "result")
        int duplicateOut(@Param("result") int value);
    }

    public static class CallableMethods {
        public static int doubleValue(int value) {
            return value * 2;
        }

        public static String greeting(String name) {
            return "Hello " + name;
        }
    }

    private static class FakeCallableDataSource implements DataSource {
        private final Object result;
        private String sql;
        private final List<String> calls = new ArrayList<>();

        private FakeCallableDataSource(Object result) {
            this.result = result;
        }

        @Override
        public Connection getConnection() {
            InvocationHandler handler = (proxy, method, args) -> {
                if (method.getName().equals("prepareCall")) {
                    sql = (String) args[0];
                    return callableStatement();
                }
                return defaultValue(method.getReturnType());
            };
            return proxy(Connection.class, handler);
        }

        private CallableStatement callableStatement() {
            InvocationHandler handler = (proxy, method, args) -> {
                switch (method.getName()) {
                    case "registerOutParameter" -> calls.add("registerOutParameter(" + args[0] + "," + args[1] + ")");
                    case "setObject" -> calls.add("setObject(" + args[0] + "," + args[1] + ")");
                    case "execute" -> {
                        calls.add("execute()");
                        return true;
                    }
                    case "executeUpdate" -> {
                        calls.add("executeUpdate()");
                        return result;
                    }
                    case "getObject" -> {
                        calls.add("getObject(" + args[0] + ")");
                        return result;
                    }
                    default -> {
                        return defaultValue(method.getReturnType());
                    }
                }
                return null;
            };
            return proxy(CallableStatement.class, handler);
        }

        @Override
        public Connection getConnection(String username, String password) {
            return getConnection();
        }

        @Override
        public java.io.PrintWriter getLogWriter() {
            return null;
        }

        @Override
        public void setLogWriter(java.io.PrintWriter out) {
        }

        @Override
        public void setLoginTimeout(int seconds) {
        }

        @Override
        public int getLoginTimeout() {
            return 0;
        }

        @Override
        public java.util.logging.Logger getParentLogger() {
            return java.util.logging.Logger.getGlobal();
        }

        @Override
        public <T> T unwrap(Class<T> iface) {
            return null;
        }

        @Override
        public boolean isWrapperFor(Class<?> iface) {
            return false;
        }

        @SuppressWarnings("unchecked")
        private static <T> T proxy(Class<T> type, InvocationHandler handler) {
            return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[]{type}, handler);
        }

        private static Object defaultValue(Class<?> type) {
            if (type == boolean.class) return false;
            if (type == byte.class) return (byte) 0;
            if (type == short.class) return (short) 0;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0f;
            if (type == double.class) return 0d;
            if (type == char.class) return '\0';
            return null;
        }
    }
}
