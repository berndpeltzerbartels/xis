package one.xis;

import java.lang.annotation.*;

/**
 * Injects a value supplied by a child {@code <xis:parameter>} element into an
 * action method parameter.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionParameter {
    String value();
}
