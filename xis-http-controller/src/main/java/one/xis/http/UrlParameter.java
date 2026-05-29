package one.xis.http;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Injects a query-string parameter into a plain HTTP controller method parameter.
 *
 * <p>For a request such as {@code /api/customers?status=active}, use
 * {@code @UrlParameter("status")}. The value is converted to the parameter type with XIS simple type conversion.</p>
 */
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.PARAMETER)
public @interface UrlParameter {
    /**
     * Name of the query parameter to read.
     */
    String value();
}
