package one.xis.auth;

import one.xis.validation.Validate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for local login parameters.
 * <p>
 * This annotation is used to mark parameters in methods that handle local login requests.
 * It indicates that the parameter should be extracted from the request body or URL.
 */
@Retention(RUNTIME)
@Target(TYPE)
@Validate(messageKey = "invalid.credentials", globalMessageKey = "invalid.credentials.global", validatorClass = LoginValidator.class)
public @interface Login {
}
