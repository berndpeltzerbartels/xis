package one.xis.http;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a plain HTTP controller method to an HTTP {@code POST} route.
 *
 * <p>Use this for endpoints that create resources, trigger commands, or receive webhook payloads. The value is the
 * route path, optionally relative to the controller base path.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Post {
    /**
     * Route path relative to the controller base path.
     */
    String value();
}
