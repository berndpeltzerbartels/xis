package one.xis.http;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a plain HTTP controller method to an HTTP {@code OPTIONS} route.
 *
 * <p>Use this when an application needs an explicit capabilities or preflight-style endpoint. The built-in CORS filter
 * may answer ordinary CORS preflight requests before controller invocation.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Options {
    /**
     * Route path relative to the controller base path.
     */
    String value();
}
