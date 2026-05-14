package one.xis.sql;

import one.xis.context.UseAdvice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Executes an interface method in a JDBC transaction.
 *
 * <p>If the method completes normally, the transaction is committed. If the
 * method throws an exception, the transaction is rolled back. Nested
 * transactional calls join the already active transaction.</p>
 *
 * <p>XIS Boot applies this annotation through interface advice. Put it on a
 * service interface, a service interface method, an implementing class, or an
 * implementing method, and inject the service through its interface.
 * Repository interfaces and default methods can also use this annotation for
 * repository-local transaction boundaries.</p>
 */
@UseAdvice(SqlTransactionAdvice.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Transactional {
}
