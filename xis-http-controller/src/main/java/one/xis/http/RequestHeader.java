package one.xis.http;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Injects an HTTP request header value into a plain HTTP controller method parameter.
 *
 * <p>The value is converted to the parameter type with XIS simple type conversion.</p>
 */
@Retention(RUNTIME)
@Target({PARAMETER, METHOD})
public @interface RequestHeader {
    /**
     * Name of the request header to read.
     */
    String value();

}
