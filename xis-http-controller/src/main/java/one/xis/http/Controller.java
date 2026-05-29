package one.xis.http;

import one.xis.context.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a plain HTTP controller.
 *
 * <p>Plain HTTP controllers are intended for connecting external applications, scripts, webhooks, and integration
 * partners to a XIS application. They are deliberately separate from page and frontlet controllers: methods annotated
 * with {@link Get}, {@link Post}, {@link Put}, {@link Delete}, {@link Head}, {@link Options}, or {@link Trace} do not
 * render templates. Their return values are written directly to the HTTP response body, or wrapped in
 * {@link ResponseEntity} when status codes and dynamic headers are needed.</p>
 *
 * <p>The optional value is a base path that is prepended to all route annotations in the controller.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface Controller {
    /**
     * Base path for all HTTP route methods in this controller.
     */
    String value() default "";
}
