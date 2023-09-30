package one.xis.context;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Optional;

class ProxyUtils {

    @SuppressWarnings("unchecked")
    static Optional<Class<? extends ProxyFactory<?>>> factoryClass(Class<?> c) {
        return proxyAnnotation(c)
                .map(XISProxy::factory)
                .map(factory -> (Class<? extends ProxyFactory<?>>) factory);
    }


    static boolean requiresProxy(Class<?> c) {
        if (!c.isInterface()) {
            return false;
        }
        return Arrays.stream(c.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().isAnnotationPresent(XISProxy.class));
    }


    static Optional<XISProxy> proxyAnnotation(Class<?> interf) {
        return Arrays.stream(interf.getAnnotations())
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(XISProxy.class))
                .map(Annotation::annotationType)
                .map(interf::getAnnotation)
                .map(Annotation::annotationType)
                .map(type -> type.getAnnotation(XISProxy.class))
                .findFirst();
    }
}
