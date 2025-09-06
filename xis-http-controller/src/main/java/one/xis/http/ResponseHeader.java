package one.xis.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that a method parameter or method return value should be bound to a specific HTTP response header.
 * This is typically used in RESTful web services to set values in HTTP response headers.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ResponseHeader {
    String name();

    String value();
}
