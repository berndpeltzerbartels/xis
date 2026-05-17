package one.xis.boot.nativeimage;

import one.xis.context.AppContext;
import one.xis.context.NativeAppContextBuilderImpl;
import one.xis.context.NoProxyFactoryClass;
import one.xis.context.Proxy;
import one.xis.context.ProxyFactory;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;

/**
 * Builds a XIS application context from generated native component catalogs.
 * <p>
 * This builder intentionally does not accept packages to scan. Native startup
 * is based on explicit class references generated at build time.
 */
public class NativeAppContextBuilder {

    private final NativeAppContextBuilderImpl delegate = new NativeAppContextBuilderImpl();

    public NativeAppContextBuilder withRegistry(NativeComponentRegistry registry) {
        return withComponentClasses(registry.componentClasses());
    }

    public NativeAppContextBuilder withRegistries(NativeComponentRegistry... registries) {
        Arrays.stream(registries).forEach(this::withRegistry);
        return this;
    }

    public NativeAppContextBuilder withRegistries(Collection<NativeComponentRegistry> registries) {
        registries.forEach(this::withRegistry);
        return this;
    }

    public NativeAppContextBuilder withComponentClasses(Collection<Class<?>> componentClasses) {
        componentClasses.forEach(this::withComponentClass);
        return this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void withComponentClass(Class<?> componentClass) {
        if (componentClass.isInterface()) {
            registerProxyInterface(componentClass);
            return;
        }
        delegate.withSingletonClass(componentClass);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void registerProxyInterface(Class<?> proxyInterface) {
        for (var annotation : proxyInterface.getAnnotations()) {
            var proxy = annotation.annotationType().getAnnotation(Proxy.class);
            if (proxy == null) {
                continue;
            }
            // Native startup has no Reflections package scan, so proxy stereotypes such as
            // @Repository must be made visible to the context from the generated class catalog.
            delegate.withProxyInterface(
                    (Class<? extends Annotation>) annotation.annotationType(),
                    (Class<? extends ProxyFactory<?>>) proxyFactoryClass(proxy),
                    proxyInterface
            );
        }
    }

    private Class<? extends ProxyFactory> proxyFactoryClass(Proxy proxy) {
        if (!proxy.factory().equals(NoProxyFactoryClass.class)) {
            return proxy.factory();
        }
        try {
            return (Class<? extends ProxyFactory>) Class.forName(proxy.factoryName());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not load proxy factory " + proxy.factoryName(), e);
        }
    }

    public NativeAppContextBuilder withSingleton(Object singleton) {
        delegate.withSingleton(singleton);
        return this;
    }

    public AppContext build() {
        return delegate.build();
    }
}
