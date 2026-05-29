package one.xis.http;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a plain HTTP controller method to an HTTP {@code TRACE} route.
 *
 * <p>TRACE is rarely needed in application code. Expose it only when an integration or diagnostic endpoint explicitly
 * requires it.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Trace {
    /**
     * Route path relative to the controller base path.
     */
    String value();
}
