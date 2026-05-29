package one.xis.http;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a plain HTTP controller method to an HTTP {@code HEAD} route.
 *
 * <p>Use this for metadata-only checks, for example to expose whether a resource exists without returning its body.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Head {
    /**
     * Route path relative to the controller base path.
     */
    String value();
}
