package one.xis.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;

/**
 *
 */
@Target(ElementType.ANNOTATION_TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface XISProxy {
    /**
     * @return Qualified classname of an {@link java.lang.reflect.InvocationHandler}
     */
    String handlerName() default "";

    Class<? extends InvocationHandler> handlerClass() default NoInvocationHandler.class;
}
