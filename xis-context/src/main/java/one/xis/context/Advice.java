package one.xis.context;

/**
 * Intercepts a method invocation on a XIS interface proxy.
 */
@FunctionalInterface
public interface Advice {

    /**
     * Handles a proxied invocation. Implementations should call {@link AdviceInvocation#proceed()} unless they
     * deliberately want to replace or suppress the target method call.
     *
     * @param invocation invocation context
     * @return the target method result
     * @throws Throwable any exception thrown by the advice or the target method
     */
    Object around(AdviceInvocation invocation) throws Throwable;
}
