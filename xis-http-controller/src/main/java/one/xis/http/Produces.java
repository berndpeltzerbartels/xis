package one.xis.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate the content type that a method produces in response to an HTTP request.
 * This is typically used in RESTful web services to specify the format of the response body,
 * such as JSON or XML.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Produces {
    ContentType value();
}
