package one.xis.sql;

record PoolConfiguration(
        String url,
        String user,
        String password,
        String driverClassName,
        int maximumPoolSize,
        int minimumIdle,
        long connectionTimeout,
        long idleTimeout,
        long maxLifetime
) {
}
