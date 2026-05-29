package one.xis.http;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a plain HTTP controller method to an HTTP {@code GET} route.
 *
 * <p>Use this for endpoints that read data for external applications, scripts, webhooks, or integration partners. The
 * value is the route path, optionally containing path variables such as {@code /api/customers/{id}}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Get {
    /**
     * Route path relative to the controller base path.
     */
    String value();
}
