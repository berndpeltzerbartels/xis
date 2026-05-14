package one.xis.context;

import lombok.Data;

@Data
class ProxyFactoryAnnotationMapping {
    private final Class<?> annotation;
    private final Class<?> proxyFactory;
}
