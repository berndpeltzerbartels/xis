package one.xis.context2;

import lombok.Data;

@Data
class ProxyFactoryAnnotationMapping {
    private final Class<?> annotation;
    private final Class<?> proxyFactory;
}
