package one.xis.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Requires a value to look like an email address.
 *
 * <p>This validator is intentionally lightweight: it catches ordinary user input mistakes but does not try to prove that
 * an address exists or that every theoretically valid RFC edge case is accepted. Combine it with
 * {@link one.xis.validation.Mandatory} when the field must not be empty.</p>
 */
@Retention(RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Validate(validatorClass = EMailValidator.class, messageKey = "validation.email", globalMessageKey = "validation.email.global")
public @interface EMail {
}
