package one.xis;

import java.lang.annotation.*;

/**
 * Marks a router method as a route mapping.
 *
 * <p>The route value is appended to the surrounding {@link Router} path.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Route {
    String value() default "";
}
