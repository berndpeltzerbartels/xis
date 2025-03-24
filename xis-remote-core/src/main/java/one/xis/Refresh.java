package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * WHen a method is annotated with @Refresh, the controller identified by the
 * controllerClass or controllerName will be refreshed after the method has been executed.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Refresh {
    Class<?> controllerCLass() default Void.class;

    String controllerName() default "";
}
