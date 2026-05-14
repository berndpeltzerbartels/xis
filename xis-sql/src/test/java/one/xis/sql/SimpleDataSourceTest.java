package one.xis.sql;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SimpleDataSourceTest {

    @Test
    void createsPooledDataSourceWhenPoolingIsEnabled() throws Exception {
        SimpleDataSource dataSource = new SimpleDataSource();
        set(dataSource, "url", "jdbc:h2:mem:pooled-default-datasource;DB_CLOSE_DELAY=-1");
        set(dataSource, "poolEnabled", true);

        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("create table pooled_connection_test (id int primary key)");
            statement.execute("insert into pooled_connection_test values (1)");

            try (var resultSet = statement.executeQuery("select count(*) from pooled_connection_test")) {
                assertTrue(resultSet.next());
                assertEquals(1, resultSet.getInt(1));
            }
        }

        assertEquals("com.zaxxer.hikari.HikariDataSource", pooledDataSource(dataSource).getClass().getName());
        closePooledDataSource(dataSource);
    }

    @Test
    void usesPoolingDefaultsWhenPoolPropertiesAreMissing() throws Exception {
        SimpleDataSource dataSource = new SimpleDataSource();
        set(dataSource, "url", "jdbc:h2:mem:pooled-defaults;DB_CLOSE_DELAY=-1");
        set(dataSource, "poolEnabled", true);

        dataSource.validateConfiguration();
        dataSource.getConnection().close();

        Object pooledDataSource = pooledDataSource(dataSource);
        assertEquals(10, invokeInt(pooledDataSource, "getMaximumPoolSize"));
        assertEquals(2, invokeInt(pooledDataSource, "getMinimumIdle"));
        assertEquals(30_000L, invokeLong(pooledDataSource, "getConnectionTimeout"));
        assertEquals(600_000L, invokeLong(pooledDataSource, "getIdleTimeout"));
        assertEquals(1_800_000L, invokeLong(pooledDataSource, "getMaxLifetime"));
        closePooledDataSource(dataSource);
    }

    @Test
    void rejectsInvalidPoolProperties() throws Exception {
        SimpleDataSource dataSource = new SimpleDataSource();
        set(dataSource, "url", "jdbc:h2:mem:invalid-pool;DB_CLOSE_DELAY=-1");
        set(dataSource, "poolEnabled", true);
        set(dataSource, "maximumPoolSize", 1);
        set(dataSource, "minimumIdle", 2);

        var exception = assertThrows(IllegalStateException.class, dataSource::validateConfiguration);

        assertEquals("Property 'xis.sql.pool.minimum-idle' must not be greater than 'xis.sql.pool.maximum-pool-size'", exception.getMessage());
    }

    @Test
    void stillUsesDriverManagerWithoutPooling() throws Exception {
        SimpleDataSource dataSource = new SimpleDataSource();
        set(dataSource, "url", "jdbc:h2:mem:simple-default-datasource;DB_CLOSE_DELAY=-1");

        try (var connection = dataSource.getConnection()) {
            assertTrue(connection.isValid(1));
        }

        assertThrows(IllegalStateException.class, () -> pooledDataSource(dataSource));
    }

    @Test
    void reusesConnectionInRequestOnlyWithoutPooling() throws Exception {
        SimpleDataSource simpleDataSource = new SimpleDataSource();
        SimpleDataSource pooledDataSource = new SimpleDataSource();
        set(simpleDataSource, "poolEnabled", false);
        set(pooledDataSource, "poolEnabled", true);

        assertTrue(simpleDataSource.shouldReuseConnectionInRequest());
        assertEquals(false, pooledDataSource.shouldReuseConnectionInRequest());
    }

    private static Object pooledDataSource(SimpleDataSource dataSource) throws Exception {
        Field field = SimpleDataSource.class.getDeclaredField("pooledDataSource");
        field.setAccessible(true);
        Object value = field.get(dataSource);
        if (value == null) {
            throw new IllegalStateException("No pooled DataSource was created");
        }
        return value;
    }

    private static int invokeInt(Object target, String methodName) throws Exception {
        return (Integer) target.getClass().getMethod(methodName).invoke(target);
    }

    private static long invokeLong(Object target, String methodName) throws Exception {
        return (Long) target.getClass().getMethod(methodName).invoke(target);
    }

    private static void closePooledDataSource(SimpleDataSource dataSource) throws Exception {
        Object pooledDataSource = pooledDataSource(dataSource);
        pooledDataSource.getClass().getMethod("close").invoke(pooledDataSource);
    }

    private static void set(SimpleDataSource target, String fieldName, Object value) throws Exception {
        Field field = SimpleDataSource.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
