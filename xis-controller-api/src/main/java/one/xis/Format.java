package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation allows to control output formatting in input parsing by specifying
 * a custom {@link Formatter} for a field.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Format {
    Class<? extends Formatter<?>> value();
}
