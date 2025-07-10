package one.xis.http;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to indicate that a method parameter should be bound to a specific HTTP header.
 * This is typically used in RESTful web services to retrieve values from HTTP headers.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface Header {
    String value();

}
