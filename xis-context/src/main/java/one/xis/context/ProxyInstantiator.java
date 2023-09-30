package one.xis.context;

import lombok.RequiredArgsConstructor;
import one.xis.utils.lang.ClassUtils;

import java.util.Collection;
import java.util.HashSet;

@RequiredArgsConstructor
class ProxyInstantiator implements ComponentProducer, ComponentConsumer {

    private final Class<Object> interf;
    private final Class<? extends ProxyFactory<?>> proxyFactoryClass;
    private final AppContextFactory contextFactory;
    private final Collection<ComponentCreationListener> componentCreationListeners = new HashSet<>();
    private ProxyFactory<Object> proxyFactory;

    @Override
    public void mapProducers(Collection<ComponentProducer> producers) {
        producers.stream()
                .filter(producer -> ClassUtils.related(producer.getResultClass(), proxyFactoryClass))
                .forEach(this::mapProducer);
    }

    @Override
    public void mapInitialComponents(Collection<Object> components) {
        components.stream().filter(proxyFactoryClass::isInstance)
                .map(ProxyFactory.class::cast)
                .forEach(factory -> {
                    if (proxyFactory != null) {
                        throw new IllegalStateException("too many candidates for " + proxyFactoryClass);
                    }
                    proxyFactory = factory;
                    createProxy();
                });
    }

    @SuppressWarnings("unchecked")
    private void mapProducer(ComponentProducer componentProducer) {
        componentProducer.addComponentCreationListener((Object o, ComponentProducer producer) -> {
            if (proxyFactoryClass.isInstance(o)) {
                if (proxyFactory != null) {
                    throw new IllegalStateException("too many candidates for " + proxyFactoryClass);
                }
                proxyFactory = (ProxyFactory<Object>) o;
                createProxy();
            }
        });
    }

    private void createProxy() {
        if (contextFactory.getReplacedClasses().contains(interf)) {
            componentCreationListeners.forEach(componentCreationListener -> componentCreationListener.componentCreated(new Empty(), this));
        } else {
            var proxy = proxyFactory.createProxy(interf);
            componentCreationListeners.forEach(componentCreationListener -> componentCreationListener.componentCreated(proxy, this));
        }
    }

    @Override
    public void addComponentCreationListener(ComponentCreationListener listener) {
        componentCreationListeners.add(listener);
    }

    @Override
    public Class<?> getResultClass() {
        return interf;
    }
}
