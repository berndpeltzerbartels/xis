package one.xis.http;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Binds the HTTP request body to a plain HTTP controller method parameter.
 *
 * <p>The default body type is {@link BodyType#JSON}. Text bodies are converted with simple type conversion, and
 * form-url-encoded bodies can be mapped to an object by field name.</p>
 */
@Retention(RUNTIME)
@Target(PARAMETER)
public @interface RequestBody {
    /**
     * How the request body should be decoded before it is injected.
     */
    BodyType value() default BodyType.JSON;
}
