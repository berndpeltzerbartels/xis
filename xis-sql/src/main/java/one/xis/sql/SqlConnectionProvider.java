package one.xis.sql;

import lombok.extern.slf4j.Slf4j;
import one.xis.context.Component;
import one.xis.http.RequestContext;
import one.xis.http.RequestContextResource;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
@Slf4j
class SqlConnectionProvider {
    private static final String REQUEST_STATE_KEY = SqlConnectionProvider.class.getName() + ".state";

    private final ThreadLocal<SqlConnectionState> threadState = new ThreadLocal<>();

    Connection getConnection(DataSource dataSource) throws SQLException {
        SqlConnectionState state = existingState();
        if (state == null && !shouldReuseConnectionInRequest(dataSource)) {
            return statementLoggingConnection(dataSource.getConnection());
        }
        return currentState().getConnection(new ConnectionKey(dataSource, null, null));
    }

    Connection getConnection(DataSource dataSource, String username, String password) throws SQLException {
        SqlConnectionState state = existingState();
        if (state == null && !shouldReuseConnectionInRequest(dataSource)) {
            return statementLoggingConnection(dataSource.getConnection(username, password));
        }
        return currentState().getConnection(new ConnectionKey(dataSource, username, password));
    }

    SqlConnectionState currentState() {
        SqlConnectionState state = existingState();
        if (state != null) {
            return state;
        }
        RequestContext requestContext = RequestContext.getInstance();
        if (requestContext != null) {
            return createRequestState(requestContext);
        }
        state = new SqlConnectionState(this::clearThreadState);
        threadState.set(state);
        return state;
    }

    void closeThreadStateIfNoRequest(Throwable failure) {
        if (RequestContext.getInstance() != null) {
            return;
        }
        SqlConnectionState state = threadState.get();
        if (state != null) {
            state.close(failure);
        }
    }

    SqlConnectionState existingState() {
        RequestContext requestContext = RequestContext.getInstance();
        if (requestContext != null) {
            return (SqlConnectionState) requestContext.getAttribute(REQUEST_STATE_KEY);
        }
        return threadState.get();
    }

    private SqlConnectionState createRequestState(RequestContext requestContext) {
        var state = new SqlConnectionState(() -> {
        });
        requestContext.setAttribute(REQUEST_STATE_KEY, state);
        requestContext.registerResource(state);
        return state;
    }


    private void clearThreadState() {
        threadState.remove();
    }

    private boolean shouldReuseConnectionInRequest(DataSource dataSource) {
        if (RequestContext.getInstance() == null) {
            return false;
        }
        SimpleDataSource simpleDataSource = simpleDataSource(dataSource);
        return simpleDataSource != null && simpleDataSource.shouldReuseConnectionInRequest();
    }

    private SimpleDataSource simpleDataSource(DataSource dataSource) {
        if (dataSource instanceof SimpleDataSource simpleDataSource) {
            return simpleDataSource;
        }
        try {
            if (dataSource.isWrapperFor(SimpleDataSource.class)) {
                return dataSource.unwrap(SimpleDataSource.class);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not inspect SQL DataSource", e);
        }
        return null;
    }

    private static Connection statementLoggingConnection(Connection connection) {
        return connectionProxy(connection, false);
    }

    private static Connection nonClosingConnection(Connection connection) {
        return connectionProxy(connection, true);
    }

    private static Connection connectionProxy(Connection connection, boolean ignoreClose) {
        return (Connection) Proxy.newProxyInstance(
                connection.getClass().getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> invoke(connection, method, args, ignoreClose));
    }

    private static Object invoke(Connection connection, Method method, Object[] args, boolean ignoreClose) throws Throwable {
        if (ignoreClose && method.getName().equals("close")) {
            return null;
        }
        logSqlStatement(method, args);
        try {
            return method.invoke(connection, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private static void logSqlStatement(Method method, Object[] args) {
        if (!log.isDebugEnabled() || args == null || args.length == 0 || !(args[0] instanceof String sql)) {
            return;
        }
        if (method.getName().equals("prepareStatement")) {
            log.debug("Preparing SQL statement: {}", sql);
        } else if (method.getName().equals("prepareCall")) {
            log.debug("Preparing SQL call: {}", sql);
        }
    }

    static class SqlConnectionState implements RequestContextResource {
        private final Map<ConnectionKey, ConnectionHolder> connections = new HashMap<>();
        private final Runnable closeCallback;
        private int transactionDepth;
        private boolean rollbackOnly;
        private Throwable rollbackCause;

        SqlConnectionState(Runnable closeCallback) {
            this.closeCallback = closeCallback;
        }

        Connection getConnection(ConnectionKey key) throws SQLException {
            ConnectionHolder holder = connections.get(key);
            if (holder == null) {
                holder = new ConnectionHolder(openConnection(key));
                connections.put(key, holder);
                log.debug("Opened SQL connection for {}", key);
                if (isTransactionActive()) {
                    holder.beginTransaction();
                }
            }
            return nonClosingConnection(holder.connection);
        }

        void beginTransaction() {
            transactionDepth++;
            log.debug("Begin SQL transaction, depth={}", transactionDepth);
            for (ConnectionHolder holder : connections.values()) {
                holder.beginTransaction();
            }
        }

        void commitTransaction() {
            requireActiveTransaction("commit");
            transactionDepth--;
            log.debug("Commit SQL transaction requested, remaining depth={}", transactionDepth);
            if (transactionDepth > 0) {
                return;
            }
            try {
                if (rollbackOnly) {
                    rollbackConnections();
                } else {
                    commitConnections();
                }
                endTransaction();
            } catch (RuntimeException e) {
                markRollbackOnly(e);
                throw e;
            }
        }

        void rollbackTransaction() {
            requireActiveTransaction("rollback");
            log.debug("Rollback SQL transaction requested, depth={}", transactionDepth);
            transactionDepth = 0;
            rollbackConnections();
            endTransaction();
        }

        boolean isTransactionActive() {
            return transactionDepth > 0;
        }

        void markRollbackOnly(Throwable cause) {
            if (!isTransactionActive()) {
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Mark SQL transaction rollback-only: {}", cause.toString());
            }
            rollbackOnly = true;
            if (rollbackCause == null) {
                rollbackCause = cause;
            }
        }

        @Override
        public void close(Throwable failure) {
            try {
                if (isTransactionActive()) {
                    log.debug("Closing SQL connection state with active transaction, failure={}, rollbackOnly={}",
                            failure != null, rollbackOnly);
                    if (failure == null && !rollbackOnly) {
                        commitConnections();
                    } else {
                        rollbackConnections();
                    }
                }
                closeConnections();
            } finally {
                connections.clear();
                transactionDepth = 0;
                rollbackOnly = false;
                rollbackCause = null;
                closeCallback.run();
            }
        }

        private void endTransaction() {
            rollbackOnly = false;
            rollbackCause = null;
            for (ConnectionHolder holder : connections.values()) {
                holder.endTransaction();
            }
        }

        private void commitConnections() {
            for (ConnectionHolder holder : connections.values()) {
                log.debug("Committing SQL connection");
                holder.commit();
            }
        }

        private void rollbackConnections() {
            RuntimeException failure = null;
            for (ConnectionHolder holder : connections.values()) {
                try {
                    log.debug("Rolling back SQL connection");
                    holder.rollback();
                } catch (RuntimeException e) {
                    if (failure == null) {
                        failure = e;
                    } else {
                        failure.addSuppressed(e);
                    }
                }
            }
            if (failure != null) {
                if (rollbackCause != null) {
                    rollbackCause.addSuppressed(failure);
                }
                throw failure;
            }
        }

        private void closeConnections() {
            RuntimeException failure = null;
            for (ConnectionHolder holder : connections.values()) {
                try {
                    log.debug("Closing SQL connection");
                    holder.close();
                } catch (RuntimeException e) {
                    if (failure == null) {
                        failure = e;
                    } else {
                        failure.addSuppressed(e);
                    }
                }
            }
            if (failure != null) {
                throw failure;
            }
        }

        private void requireActiveTransaction(String operation) {
            if (!isTransactionActive()) {
                throw new IllegalStateException("Cannot " + operation + " SQL transaction because no transaction is active");
            }
        }

        private Connection openConnection(ConnectionKey key) throws SQLException {
            if (log.isDebugEnabled()) {
                log.debug("Opening SQL connection from DataSource {}", key.dataSource.getClass().getName());
            }
            if (key.username == null && key.password == null) {
                return key.dataSource.getConnection();
            }
            return key.dataSource.getConnection(key.username, key.password);
        }
    }

    private static final class ConnectionHolder {
        private final Connection connection;
        private boolean transactionStarted;
        private boolean originalAutoCommit;

        private ConnectionHolder(Connection connection) {
            this.connection = connection;
        }

        void beginTransaction() {
            if (transactionStarted) {
                return;
            }
            try {
                originalAutoCommit = connection.getAutoCommit();
                connection.setAutoCommit(false);
                transactionStarted = true;
            } catch (SQLException e) {
                throw new IllegalStateException("Could not start SQL transaction", e);
            }
        }

        void commit() {
            if (!transactionStarted) {
                return;
            }
            try {
                connection.commit();
            } catch (SQLException e) {
                throw new IllegalStateException("Could not commit SQL transaction", e);
            }
        }

        void rollback() {
            if (!transactionStarted) {
                return;
            }
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new IllegalStateException("Could not rollback SQL transaction", e);
            }
        }

        void endTransaction() {
            if (!transactionStarted) {
                return;
            }
            try {
                connection.setAutoCommit(originalAutoCommit);
                transactionStarted = false;
            } catch (SQLException e) {
                throw new IllegalStateException("Could not restore SQL connection auto-commit", e);
            }
        }

        void close() {
            try {
                endTransaction();
                connection.close();
            } catch (SQLException e) {
                throw new IllegalStateException("Could not close SQL connection", e);
            }
        }
    }

    private static final class ConnectionKey {
        private final DataSource dataSource;
        private final String username;
        private final String password;

        private ConnectionKey(DataSource dataSource, String username, String password) {
            this.dataSource = dataSource;
            this.username = username;
            this.password = password;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ConnectionKey other)) {
                return false;
            }
            return dataSource == other.dataSource
                    && Objects.equals(username, other.username)
                    && Objects.equals(password, other.password);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(dataSource) * 31
                    + Objects.hash(username, password);
        }

        @Override
        public String toString() {
            String dataSourceName = dataSource.getClass().getName();
            if (username == null && password == null) {
                return dataSourceName;
            }
            return dataSourceName + "[credentials]";
        }
    }
}
