package one.xis.sql;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * Minimal default {@link DataSource} for small XIS SQL applications.
 * <p>
 * Applications that provide their own {@code DataSource} automatically replace
 * this default component. The implementation can optionally create a HikariCP
 * backed pool when HikariCP is available on the application classpath.
 * <p>
 * Supported properties:
 * <ul>
 *     <li>{@code xis.sql.url} - JDBC URL, required when this datasource is used</li>
 *     <li>{@code xis.sql.user} - optional JDBC user</li>
 *     <li>{@code xis.sql.password} - optional JDBC password</li>
 *     <li>{@code xis.sql.driver-class-name} - optional driver class to load</li>
 *     <li>{@code xis.sql.pool.enabled} - optional, defaults to {@code false}</li>
 *     <li>{@code xis.sql.pool.maximum-pool-size} - optional, defaults to {@code 10}</li>
 *     <li>{@code xis.sql.pool.minimum-idle} - optional, defaults to {@code 2}</li>
 *     <li>{@code xis.sql.pool.connection-timeout} - optional, defaults to {@code 30000}</li>
 *     <li>{@code xis.sql.pool.idle-timeout} - optional, defaults to {@code 600000}</li>
 *     <li>{@code xis.sql.pool.max-lifetime} - optional, defaults to {@code 1800000}</li>
 * </ul>
 */
public class SimpleDataSource implements DataSource {
    private static final int DEFAULT_MAXIMUM_POOL_SIZE = 10;
    private static final int DEFAULT_MINIMUM_IDLE = 2;
    private static final long DEFAULT_CONNECTION_TIMEOUT = 30_000;
    private static final long DEFAULT_IDLE_TIMEOUT = 600_000;
    private static final long DEFAULT_MAX_LIFETIME = 1_800_000;

    private String url;
    private String user;
    private String username;
    private String password;
    private String driverClassName;
    private Boolean poolEnabled;
    private Integer maximumPoolSize;
    private Integer minimumIdle;
    private Long connectionTimeout;
    private Long idleTimeout;
    private Long maxLifetime;
    private PrintWriter logWriter;
    private int loginTimeout;
    private volatile DataSource pooledDataSource;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public void setPoolEnabled(Boolean poolEnabled) {
        this.poolEnabled = poolEnabled;
    }

    public void setMaximumPoolSize(Integer maximumPoolSize) {
        this.maximumPoolSize = maximumPoolSize;
    }

    public void setMinimumIdle(Integer minimumIdle) {
        this.minimumIdle = minimumIdle;
    }

    public void setConnectionTimeout(Long connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setIdleTimeout(Long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public void setMaxLifetime(Long maxLifetime) {
        this.maxLifetime = maxLifetime;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (isPoolEnabled()) {
            return getPooledDataSource().getConnection();
        }
        requireUrl();
        loadDriverIfConfigured();
        return openDriverManagerConnection(effectiveUser(), password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        if (isPoolEnabled()) {
            return getPooledDataSource().getConnection(username, password);
        }
        requireUrl();
        loadDriverIfConfigured();
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public PrintWriter getLogWriter() {
        return logWriter;
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        this.logWriter = out;
    }

    @Override
    public void setLoginTimeout(int seconds) {
        this.loginTimeout = seconds;
        DriverManager.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() {
        return loginTimeout;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("SimpleDataSource has no parent logger");
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        throw new SQLException("SimpleDataSource is not a wrapper for " + iface.getName());
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isInstance(this);
    }

    private void loadDriverIfConfigured() {
        if (driverClassName != null && !driverClassName.isBlank()) {
            try {
                Class.forName(driverClassName);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Could not load JDBC driver " + driverClassName, e);
            }
        }
    }

    private void requireUrl() {
        validateConfiguration();
    }

    public void validateConfiguration() {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("Property 'xis.sql.url' is required when the default SQL DataSource is used");
        }
        if (isPoolEnabled()) {
            validatePoolConfiguration();
            HikariDataSourceFactory.requireHikariCP();
        }
    }

    boolean shouldReuseConnectionInRequest() {
        return !isPoolEnabled();
    }

    private Connection openDriverManagerConnection(String user, String password) throws SQLException {
        if (user == null) {
            return DriverManager.getConnection(url);
        }
        return DriverManager.getConnection(url, user, password);
    }

    private DataSource getPooledDataSource() {
        DataSource current = pooledDataSource;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            if (pooledDataSource == null) {
                validateConfiguration();
                pooledDataSource = new HikariDataSourceFactory(poolConfiguration()).createDataSource();
            }
            return pooledDataSource;
        }
    }

    private PoolConfiguration poolConfiguration() {
        return new PoolConfiguration(
                url,
                effectiveUser(),
                password,
                driverClassName,
                maximumPoolSizeOrDefault(),
                minimumIdleOrDefault(),
                connectionTimeoutOrDefault(),
                idleTimeoutOrDefault(),
                maxLifetimeOrDefault()
        );
    }

    private String effectiveUser() {
        return user != null ? user : username;
    }

    private boolean isPoolEnabled() {
        return Boolean.TRUE.equals(poolEnabled);
    }

    private void validatePoolConfiguration() {
        requirePositive(maximumPoolSizeOrDefault(), "xis.sql.pool.maximum-pool-size");
        requirePositive(minimumIdleOrDefault(), "xis.sql.pool.minimum-idle");
        requirePositive(connectionTimeoutOrDefault(), "xis.sql.pool.connection-timeout");
        requirePositive(idleTimeoutOrDefault(), "xis.sql.pool.idle-timeout");
        requirePositive(maxLifetimeOrDefault(), "xis.sql.pool.max-lifetime");
        if (minimumIdleOrDefault() > maximumPoolSizeOrDefault()) {
            throw new IllegalStateException("Property 'xis.sql.pool.minimum-idle' must not be greater than 'xis.sql.pool.maximum-pool-size'");
        }
    }

    private void requirePositive(Number value, String propertyName) {
        if (value.longValue() <= 0) {
            throw new IllegalStateException("Property '" + propertyName + "' must be greater than 0");
        }
    }

    private int maximumPoolSizeOrDefault() {
        return maximumPoolSize == null ? DEFAULT_MAXIMUM_POOL_SIZE : maximumPoolSize;
    }

    private int minimumIdleOrDefault() {
        return minimumIdle == null ? DEFAULT_MINIMUM_IDLE : minimumIdle;
    }

    private long connectionTimeoutOrDefault() {
        return connectionTimeout == null ? DEFAULT_CONNECTION_TIMEOUT : connectionTimeout;
    }

    private long idleTimeoutOrDefault() {
        return idleTimeout == null ? DEFAULT_IDLE_TIMEOUT : idleTimeout;
    }

    private long maxLifetimeOrDefault() {
        return maxLifetime == null ? DEFAULT_MAX_LIFETIME : maxLifetime;
    }
}
