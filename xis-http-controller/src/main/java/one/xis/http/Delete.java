package one.xis.http;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a plain HTTP controller method to an HTTP {@code DELETE} route.
 *
 * <p>Use this for endpoints that remove or cancel resources for external applications or scripts. The value is the
 * route path, optionally containing path variables such as {@code /api/customers/{id}}.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Delete {
    /**
     * Route path relative to the controller base path.
     */
    String value();
}
