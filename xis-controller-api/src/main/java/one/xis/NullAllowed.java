package one.xis;

import java.lang.annotation.*;


/**
 * Annotation to indicate that a parameter of a controller method is allowed to be null.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface NullAllowed {

}
