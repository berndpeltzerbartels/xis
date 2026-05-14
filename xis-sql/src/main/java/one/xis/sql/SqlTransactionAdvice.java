package one.xis.sql;

import one.xis.context.Advice;
import one.xis.context.AdviceInvocation;

/**
 * XIS interface advice that executes {@link Transactional} methods inside an SQL transaction.
 */
public class SqlTransactionAdvice implements Advice {

    @Override
    public Object around(AdviceInvocation invocation) throws Throwable {
        TransactionManager transactionManager = invocation.appContext().getSingleton(TransactionManager.class);
        return transactionManager.invokeThrowing(invocation::proceed);
    }
}
