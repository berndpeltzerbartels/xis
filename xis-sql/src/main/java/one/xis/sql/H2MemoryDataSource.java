package one.xis.sql;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

class H2MemoryDataSource implements DataSource {
    private final SimpleDataSource delegate;
    private volatile Connection keepAliveConnection;
    private volatile boolean shutdownHookRegistered;

    H2MemoryDataSource(SimpleDataSource delegate) {
        this.delegate = delegate;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return nonClosingConnection(keepAliveConnection());
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return nonClosingConnection(keepAliveConnection(username, password));
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return delegate.getParentLogger();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        if (iface.isInstance(delegate)) {
            return iface.cast(delegate);
        }
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this) || iface.isInstance(delegate) || delegate.isWrapperFor(iface);
    }

    void closeKeepAliveConnection() {
        synchronized (this) {
            closeQuietly(keepAliveConnection);
            keepAliveConnection = null;
        }
    }

    private Connection keepAliveConnection() throws SQLException {
        return keepAliveConnection(null, null);
    }

    private Connection keepAliveConnection(String username, String password) throws SQLException {
        Connection current = keepAliveConnection;
        if (isUsable(current)) {
            return current;
        }
        synchronized (this) {
            if (!isUsable(keepAliveConnection)) {
                closeQuietly(keepAliveConnection);
                keepAliveConnection = username == null
                        ? delegate.getConnection()
                        : delegate.getConnection(username, password);
                registerShutdownHook();
            }
            return keepAliveConnection;
        }
    }

    private boolean isUsable(Connection connection) throws SQLException {
        return connection != null && !connection.isClosed() && connection.isValid(validationTimeoutSeconds());
    }

    private int validationTimeoutSeconds() throws SQLException {
        var loginTimeout = delegate.getLoginTimeout();
        return loginTimeout > 0 ? loginTimeout : 1;
    }

    private void registerShutdownHook() {
        if (shutdownHookRegistered) {
            return;
        }
        shutdownHookRegistered = true;
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeKeepAliveConnection, "xis-h2-memory-datasource-close"));
    }

    private static Connection nonClosingConnection(Connection connection) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> invokeConnection(connection, method, args));
    }

    private static Object invokeConnection(Connection connection, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("close")) {
            return null;
        }
        try {
            return method.invoke(connection, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private static void closeQuietly(Connection connection) {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ignored) {
            // Best-effort cleanup for the development H2 memory connection.
        }
    }
}
