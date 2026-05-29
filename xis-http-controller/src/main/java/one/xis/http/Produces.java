package one.xis.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the response content type for a plain HTTP controller method.
 *
 * <p>If this annotation is absent, XIS infers a content type from the request suffix when possible and otherwise uses
 * the default response serialization rules. Use {@link ResponseEntity} when the content type is only one part of a
 * response that also needs a status code or dynamic headers.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Produces {
    /**
     * Content type written to the HTTP response.
     */
    ContentType value();
}
