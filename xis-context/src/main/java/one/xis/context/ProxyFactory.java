package one.xis.context;

@FunctionalInterface
public interface ProxyFactory<I> {
    I createProxy(Class<I> interf);
}
