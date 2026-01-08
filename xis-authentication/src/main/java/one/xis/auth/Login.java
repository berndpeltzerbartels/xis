package one.xis.auth;

import one.xis.validation.Validate;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for local login form objects fo binding a validator to them.
 */
@Retention(RUNTIME)
@Target(TYPE)
@Validate(messageKey = "invalid.credentials", globalMessageKey = "invalid.credentials.global", validatorClass = LoginValidator.class)
public @interface Login {
}
