package one.xis.context;

import lombok.Data;

import java.util.Collection;

@Data
class PackageScanResult {
    private final Annotations annotations;
    private final Collection<Class<?>> annotatedComponentClasses;
    private final ProxyConfiguration proxyConfiguration;
}
