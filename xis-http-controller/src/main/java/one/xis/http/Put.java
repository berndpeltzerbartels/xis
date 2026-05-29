package one.xis.http;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a plain HTTP controller method to an HTTP {@code PUT} route.
 *
 * <p>Use this for endpoints that replace or upsert resources for external applications. The value is the route path,
 * optionally containing path variables.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Put {
    /**
     * Route path relative to the controller base path.
     */
    String value();
}
