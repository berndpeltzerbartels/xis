package one.xis.context;

import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

@RequiredArgsConstructor
class ProxyInstantiator implements Instantiator {

    private final Class<?> interf;
    private final Class<?> proxyFactoryClass;
    private ProxyFactory<Object> proxyFactory;
    private final Consumer<Object> componentConsumer;

    @Override
    @SuppressWarnings("unchecked")
    public void onComponentCreated(Object o) {
        if (proxyFactoryClass.isInstance(o)) {
            proxyFactory = (ProxyFactory<Object>) o;
        }
    }

    @Override
    public boolean isExecutable() {
        return proxyFactory != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void createInstance() {
        var proxy = proxyFactory.createProxy((Class<Object>) interf);
        componentConsumer.accept(proxy);
    }

}
