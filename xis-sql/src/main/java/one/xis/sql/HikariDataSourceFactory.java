package one.xis.sql;

import javax.sql.DataSource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class HikariDataSourceFactory {
    private static final String HIKARI_CONFIG_CLASS = "com.zaxxer.hikari.HikariConfig";
    private static final String HIKARI_DATA_SOURCE_CLASS = "com.zaxxer.hikari.HikariDataSource";

    private final PoolConfiguration configuration;

    HikariDataSourceFactory(PoolConfiguration configuration) {
        this.configuration = configuration;
    }

    static void requireHikariCP() {
        hikariClass(HIKARI_CONFIG_CLASS);
        hikariClass(HIKARI_DATA_SOURCE_CLASS);
    }

    DataSource createDataSource() {
        Object hikariConfig = createHikariConfig();
        set(hikariConfig, "setJdbcUrl", String.class, configuration.url());
        setIfPresent(hikariConfig, "setUsername", String.class, configuration.user());
        setIfPresent(hikariConfig, "setPassword", String.class, configuration.password());
        setIfPresent(hikariConfig, "setDriverClassName", String.class, configuration.driverClassName());
        set(hikariConfig, "setMaximumPoolSize", int.class, configuration.maximumPoolSize());
        set(hikariConfig, "setMinimumIdle", int.class, configuration.minimumIdle());
        set(hikariConfig, "setConnectionTimeout", long.class, configuration.connectionTimeout());
        set(hikariConfig, "setIdleTimeout", long.class, configuration.idleTimeout());
        set(hikariConfig, "setMaxLifetime", long.class, configuration.maxLifetime());
        return createHikariDataSource(hikariConfig);
    }

    private Object createHikariConfig() {
        try {
            return hikariClass(HIKARI_CONFIG_CLASS).getConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not create HikariConfig", e);
        }
    }

    private DataSource createHikariDataSource(Object hikariConfig) {
        try {
            Class<?> dataSourceClass = hikariClass(HIKARI_DATA_SOURCE_CLASS);
            Constructor<?> constructor = dataSourceClass.getConstructor(hikariClass(HIKARI_CONFIG_CLASS));
            return (DataSource) constructor.newInstance(hikariConfig);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Could not create HikariDataSource", e);
        }
    }

    private void setIfPresent(Object target, String methodName, Class<?> parameterType, String value) {
        if (value != null && !value.isBlank()) {
            set(target, methodName, parameterType, value);
        }
    }

    private void set(Object target, String methodName, Class<?> parameterType, Object value) {
        try {
            Method method = target.getClass().getMethod(methodName, parameterType);
            method.invoke(target, value);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("HikariCP does not provide expected method " + methodName, e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Could not access HikariCP method " + methodName, e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("HikariCP rejected SQL pool configuration: " + e.getTargetException().getMessage(), e.getTargetException());
        }
    }

    private static Class<?> hikariClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "SQL connection pooling requires HikariCP on the application classpath. Add runtime dependency 'com.zaxxer:HikariCP'.",
                    e
            );
        }
    }
}
