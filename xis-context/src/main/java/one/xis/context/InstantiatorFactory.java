package one.xis.context;

import lombok.RequiredArgsConstructor;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.Consumer;
import java.util.stream.Collectors;


@RequiredArgsConstructor
class InstantiatorFactory {

    private final ParameterFactory parameterFactory;
    private final ComponentClassReflector componentClassReflector;
    private final Consumer<Object> objectConsumer;

    Instantiator createComponentInstantiator(Class<?> c) {
        if (ProxyUtils.requiresProxy(c)) {
            return createProxyInstantiator(c);
        } else {
            return createConstructorInstantiator(c);
        }
    }

    private ProxyInstantiator createProxyInstantiator(Class<?> c) {
        return new ProxyInstantiator(c, ProxyUtils.factoryClass(c), objectConsumer);
    }


    private ConstructorInstantiator createConstructorInstantiator(Class<?> c) {
        var constructor = componentClassReflector.findValidConstructor(c);
        var componentParameters = parameterFactory.componentParameters(constructor).collect(Collectors.toCollection(ConcurrentLinkedDeque::new));
        return new ConstructorInstantiator(constructor, componentParameters, objectConsumer);
    }

}
