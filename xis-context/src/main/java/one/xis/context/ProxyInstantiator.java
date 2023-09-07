package one.xis.context;

import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
class ProxyInstantiator<I> implements SingletonInstantiator<I> {

    private final Class<I> interf;
    private final Class<I> proxyFactoryClass;
    private ProxyFactory<I> proxyFactory;


    @Override
    public void onSingletonClassesFound(Set<Class<?>> singletonClasses) {
        // Not needed, here
    }

    @Override
    public Class<?> getType() {
        return interf;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onComponentCreated(Object o) {
        if (proxyFactoryClass.isInstance(o)) {
            proxyFactory = (ProxyFactory<I>) o;
        }
    }

    @Override
    public boolean isParameterCompleted() {
        return proxyFactory != null;
    }

    @Override
    public I createInstance() {
        return proxyFactory.createProxy(interf);
    }

}
