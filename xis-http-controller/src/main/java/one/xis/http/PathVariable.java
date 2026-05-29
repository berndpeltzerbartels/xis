package one.xis.http;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Injects a variable from a plain HTTP route path into a controller method parameter.
 *
 * <p>The annotation belongs to {@code one.xis.http}. It is different from the page/frontlet
 * {@code one.xis.PathVariable} annotation. Use it with route templates such as
 * {@code @Get("/api/customers/{id}")}.</p>
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.PARAMETER)
public @interface PathVariable {
    /**
     * Name of the placeholder in the route path.
     */
    String value();
}
