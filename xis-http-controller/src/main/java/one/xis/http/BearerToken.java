package one.xis.http;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to indicate that a method parameter should be bound to the Bearer token from the HTTP Authorization header.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface BearerToken {
    boolean optional() default false;

}
