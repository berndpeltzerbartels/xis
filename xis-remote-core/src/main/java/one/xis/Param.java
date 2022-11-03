package one.xis;

import java.lang.annotation.*;

/**
 * Identifies a parameter of a controller-method passed form parent contrainer.
 * Must be a primitve or a string.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface Param {
    boolean mandatory() default true; // TODO Brauch man das ?
}
