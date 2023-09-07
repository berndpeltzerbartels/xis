package one.xis.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface XISProxy {
    Class<? extends ProxyFactory> factory() default NoProxyFactoryClass.class;

    String factoryName() default "";
}
