package one.xis.http;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to indicate that a method should handle HTTP PUT requests.
 * This is typically used in RESTful web services to retrieve resources.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Put {
    String value();
}
