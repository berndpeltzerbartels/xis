package one.xis.sql;

import one.xis.context.Component;

@Component
class TransactionManager {
    private final SqlConnectionProvider connectionProvider;

    TransactionManager(SqlConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    Object invoke(TransactionalInvocation invocation) {
        try {
            return invokeThrowing(invocation::invoke);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new IllegalStateException("Unexpected checked exception in SQL transaction", e);
        }
    }

    Object invokeThrowing(ThrowingTransactionalInvocation invocation) throws Throwable {
        boolean startedTransaction = !isActive();
        if (startedTransaction) {
            begin();
        }
        try {
            Object result = invocation.invoke();
            if (startedTransaction) {
                commit();
            }
            return result;
        } catch (Throwable e) {
            if (startedTransaction) {
                rollback();
            } else {
                markRollbackOnly(e);
            }
            throw e;
        }
    }

    void begin() {
        connectionProvider.currentState().beginTransaction();
    }

    void commit() {
        RuntimeException failure = null;
        try {
            connectionProvider.currentState().commitTransaction();
        } catch (RuntimeException e) {
            failure = e;
            throw e;
        } finally {
            connectionProvider.closeThreadStateIfNoRequest(failure);
        }
    }

    void rollback() {
        RuntimeException failure = null;
        try {
            connectionProvider.currentState().rollbackTransaction();
        } catch (RuntimeException e) {
            failure = e;
            throw e;
        } finally {
            connectionProvider.closeThreadStateIfNoRequest(failure);
        }
    }

    boolean isActive() {
        SqlConnectionProvider.SqlConnectionState state = connectionProvider.existingState();
        return state != null && state.isTransactionActive();
    }

    void markRollbackOnly(Throwable cause) {
        SqlConnectionProvider.SqlConnectionState state = connectionProvider.existingState();
        if (state != null) {
            state.markRollbackOnly(cause);
        }
    }

    interface TransactionalInvocation {
        Object invoke();
    }

    interface ThrowingTransactionalInvocation {
        Object invoke() throws Throwable;
    }
}
