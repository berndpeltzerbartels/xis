package one.xis.sql;

import one.xis.context.Component;
import one.xis.context.Value;

import javax.sql.DataSource;
import java.util.Locale;

@Component
class DataSourceFactory {

    @Value(value = "xis.sql.url", mandatory = false)
    private String url;
    @Value(value = "xis.sql.user", mandatory = false)
    private String user;
    @Value(value = "xis.sql.username", mandatory = false)
    private String username;
    @Value(value = "xis.sql.password", mandatory = false)
    private String password;
    @Value(value = "xis.sql.driver-class-name", mandatory = false)
    private String driverClassName;
    @Value(value = "xis.sql.pool.enabled", mandatory = false)
    private Boolean poolEnabled;
    @Value(value = "xis.sql.pool.maximum-pool-size", mandatory = false)
    private Integer maximumPoolSize;
    @Value(value = "xis.sql.pool.minimum-idle", mandatory = false)
    private Integer minimumIdle;
    @Value(value = "xis.sql.pool.connection-timeout", mandatory = false)
    private Long connectionTimeout;
    @Value(value = "xis.sql.pool.idle-timeout", mandatory = false)
    private Long idleTimeout;
    @Value(value = "xis.sql.pool.max-lifetime", mandatory = false)
    private Long maxLifetime;

    DataSource dataSource() {
        var dataSource = simpleDataSource(effectiveUrl());
        dataSource.validateConfiguration();
        if (isH2MemoryUrl() && !isPoolEnabled()) {
            return new H2MemoryDataSource(dataSource);
        }
        return dataSource;
    }

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

    private SimpleDataSource simpleDataSource(String dataSourceUrl) {
        var dataSource = new SimpleDataSource();
        dataSource.setUrl(dataSourceUrl);
        dataSource.setUser(user);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName(driverClassName);
        dataSource.setPoolEnabled(poolEnabled);
        dataSource.setMaximumPoolSize(maximumPoolSize);
        dataSource.setMinimumIdle(minimumIdle);
        dataSource.setConnectionTimeout(connectionTimeout);
        dataSource.setIdleTimeout(idleTimeout);
        dataSource.setMaxLifetime(maxLifetime);
        return dataSource;
    }

    private String effectiveUrl() {
        if (isH2MemoryUrl() && !hasDbCloseDelay()) {
            return url + ";DB_CLOSE_DELAY=-1";
        }
        return url;
    }

    private boolean isH2MemoryUrl() {
        var normalizedUrl = normalizedUrl();
        return normalizedUrl != null && normalizedUrl.startsWith("jdbc:h2:mem:");
    }

    private boolean hasDbCloseDelay() {
        var normalizedUrl = normalizedUrl();
        return normalizedUrl != null && normalizedUrl.contains("db_close_delay=");
    }

    private boolean isPoolEnabled() {
        return Boolean.TRUE.equals(poolEnabled);
    }

    private String normalizedUrl() {
        return url == null ? null : url.toLowerCase(Locale.ROOT);
    }
}
