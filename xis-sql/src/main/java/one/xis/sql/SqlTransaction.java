package one.xis.sql;

import one.xis.context.Component;

/**
 * Explicit transaction boundary for XIS SQL repository operations.
 *
 * <p>The transaction is bound to the current request/thread. Calling
 * {@link #begin()} does not open a JDBC connection immediately. The connection
 * is opened lazily when a repository method first needs it. During a web
 * request, an open transaction is committed automatically when the request
 * completes normally and rolled back when request processing fails.</p>
 *
 * <p>Use {@link #commit()} or {@link #rollback()} when a transaction must end
 * before the request ends, or outside an HTTP request.</p>
 */
@Component
public class SqlTransaction {
    private final TransactionManager transactionManager;

    SqlTransaction(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void begin() {
        transactionManager.begin();
    }

    public void commit() {
        transactionManager.commit();
    }

    public void rollback() {
        transactionManager.rollback();
    }

    public boolean isActive() {
        return transactionManager.isActive();
    }
}
