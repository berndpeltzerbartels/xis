package one.xis;

import java.lang.annotation.*;

/**
 * Identifies a parameter of a method annotated with @{@link Method}.
 * This can be a simple or a complex type from client-state.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Param {
    boolean mandatory() default true;
}
