package one.xis.context;

import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

class ProxyConfiguration {
    private final Collection<Class<? extends Annotation>> proxyAnnotations;
    private final Reflections reflections;
    private final Map<Class<? extends ProxyFactory<?>>, Collection<Class<?>>> proxyInterfaces = new HashMap<>();


    ProxyConfiguration(Collection<Class<? extends Annotation>> proxyAnnotations, Reflections reflections) {
        this.proxyAnnotations = proxyAnnotations;
        this.reflections = reflections;
        init();
    }

    Collection<Class<?>> proxyInterfacesForFactory(Class<?> factory) {
        return proxyInterfaces.get(factory);
    }

    private void init() {
        proxyAnnotations.forEach(this::configure);
    }

    private <A extends Annotation> void configure(Class<A> annotation) {
        var factoryClass = factoryClass(annotation);
        var interfaceClass = interfaceClassesForProxyAnnotation(annotation);
        proxyInterfaces.computeIfAbsent(factoryClass, f -> new HashSet<>()).addAll(getDerivedInterfaces(interfaceClass));
    }

    private <I> Set<Class<? extends I>> getDerivedInterfaces(Class<I> clazz) {
        return reflections.getSubTypesOf(clazz);
    }

    private Class<?> interfaceClassesForProxyAnnotation(Class<? extends Annotation> annotation) {
        var factoryClass = factoryClass(annotation);
        return getCreateProxyMethod(factoryClass).getReturnType();
    }

    @SuppressWarnings("unchecked")
    private <I, F extends ProxyFactory<I>> Class<F> factoryClass(Class<? extends Annotation> annotation) {
        return (Class<F>) annotation.getAnnotation(XISProxy.class).factory();
    }

    private Method getCreateProxyMethod(Class<? extends ProxyFactory<?>> factoryClass) {
        return Arrays.stream(factoryClass.getDeclaredMethods())
                .filter(m -> m.getParameterCount() == 1)
                .filter(m -> m.getParameterTypes()[0].equals(Class.class))
                .filter(m -> m.getName().equals("createProxy"))
                .findFirst().orElseThrow();
    }
}
