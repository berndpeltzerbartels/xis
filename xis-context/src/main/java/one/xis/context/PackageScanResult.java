package one.xis.context;

import lombok.Data;

import java.util.Collection;
import java.util.Map;

@Data
class PackageScanResult {
    private final Annotations annotations;
    private final Collection<Class<?>> annotatedComponentClasses;
    private final Map<Class<ProxyFactory<?>>, Collection<Class<?>>> proxyInterfacesByFactory;
}
