package one.xis.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for annotations that create context proxies.
 *
 * <p>Put {@code @Proxy} on another annotation, then put that annotation on an interface. During context initialization,
 * XIS asks the configured {@link ProxyFactory} to create the singleton proxy instance. This is how repository
 * annotations such as SQL and MongoDB repositories integrate with dependency injection.</p>
 *
 * <p>Use either {@link #factory()} for a factory class or {@link #factoryName()} for a named factory component.</p>
 *
 * @see ProxyFactory
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Proxy {
    /**
     * Factory class used to create the proxy instance.
     */
    Class<? extends ProxyFactory> factory() default NoProxyFactoryClass.class;

    /**
     * Name of a factory component used to create the proxy instance.
     */
    String factoryName() default "";
}
