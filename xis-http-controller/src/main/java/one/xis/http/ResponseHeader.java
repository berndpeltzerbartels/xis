package one.xis.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds a fixed HTTP response header to a plain HTTP controller method.
 *
 * <p>This annotation is for static method-level headers. Use {@link ResponseEntity#addHeader(String, String)} when the
 * header value depends on the request or on computed data.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ResponseHeader {
    /**
     * Header name.
     */
    String name();

    /**
     * Header value.
     */
    String value();
}
