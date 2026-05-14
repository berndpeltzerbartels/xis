package one.xis.http;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation to indicate that a method parameter should be bound to the body of the HTTP request.
 * This is typically used in RESTful web services to handle JSON or XML payloads.
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface RequestBody {
    BodyType value() default BodyType.JSON;
}
