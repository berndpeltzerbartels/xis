package one.xis.context;


/**
 * A factory for creating proxy instances of the specified interface.
 *
 * @param <I>
 * @see Proxy
 */
@FunctionalInterface
public interface ProxyFactory<I> {
    I createProxy(Class<I> interf);
}
