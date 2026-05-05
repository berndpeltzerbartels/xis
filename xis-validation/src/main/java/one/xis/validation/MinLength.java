package one.xis.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Requires a string to contain at least the configured number of characters or
 * a collection/array to contain at least the configured number of elements.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Validate(validatorClass = MinLengthValidator.class, messageKey = "validation.minLength")
public @interface MinLength {
    int value();
}
