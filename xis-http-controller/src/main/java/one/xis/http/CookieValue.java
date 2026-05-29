package one.xis.http;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Injects a request cookie value into a plain HTTP controller method parameter.
 *
 * <p>The value is converted to the parameter type with XIS simple type conversion. If the cookie is absent, the
 * converted value is usually {@code null} for reference types.</p>
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.PARAMETER)
public @interface CookieValue {
    /**
     * Name of the cookie to read.
     */
    String value();
}
