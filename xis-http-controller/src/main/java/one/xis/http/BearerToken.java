package one.xis.http;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Injects the bearer token from the HTTP {@code Authorization} header into a plain HTTP controller method parameter.
 *
 * <p>The parameter receives the token value without the {@code Bearer } prefix. If the header is missing and
 * {@link #optional()} is {@code false}, request handling fails before the controller method is called.</p>
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface BearerToken {
    /**
     * Whether a missing bearer token should be injected as {@code null}.
     */
    boolean optional() default false;

}
