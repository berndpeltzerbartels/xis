package one.xis.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An interface annotated with @Proxy indicates that it is a proxy interface.
 * The factory or factoryName attribute specifies the ProxyFactory class responsible for creating
 * proxy instances. The proxy is handled as a singleton within the application context.
 * <p>
 *
 * @see ProxyFactory
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Proxy {
    Class<? extends ProxyFactory> factory() default NoProxyFactoryClass.class;

    String factoryName() default "";
}
