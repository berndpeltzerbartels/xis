package one.xis.context;

import java.lang.annotation.Annotation;
import java.util.Arrays;

class ProxyUtils {

    @SuppressWarnings("unchecked")
    static Class<? extends ProxyFactory<?>> factoryClass(Class<?> c) {
        var annotation = proxyAnnotation(c);
        if (annotation.factory() != null) {
            return (Class<? extends ProxyFactory<?>>) annotation.factory();
        }
        return null;
    }


    static boolean requiresProxy(Class<?> c) {
        if (!c.isInterface()) {
            return false;
        }
        return Arrays.stream(c.getAnnotations())
                .anyMatch(annotation -> annotation.annotationType().isAnnotationPresent(XISProxy.class));
    }


    static XISProxy proxyAnnotation(Class<?> interf) {
        return Arrays.stream(interf.getAnnotations())
                .filter(annotation -> annotation.annotationType().isAnnotationPresent(XISProxy.class))
                .map(Annotation::annotationType)
                .map(interf::getAnnotation)
                .map(Annotation::annotationType)
                .map(type -> type.getAnnotation(XISProxy.class))
                .findFirst().orElseThrow();
    }
}
