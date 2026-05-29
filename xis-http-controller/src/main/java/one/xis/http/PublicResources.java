package one.xis.http;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Exposes static resource paths without requiring authentication.
 *
 * <p>Put this annotation on a plain HTTP controller class when files under the configured resource prefixes should be
 * served directly, for example public assets or callback helper files. Keep the prefixes narrow; this is an explicit
 * public access decision.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PublicResources {
    /**
     * Resource path prefixes that may be served publicly.
     */
    String[] value();
}
