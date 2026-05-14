package one.xis.theme.example.contact;

import one.xis.validation.Validate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation for international phone numbers.
 * Validates basic phone number format (allows +, spaces, dashes, parentheses, and digits).
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Validate(validatorClass = PhoneNumberValidator.class, 
          messageKey = "validation.phone.invalid", 
          globalMessageKey = "validation.phone.invalid.global")
public @interface PhoneNumber {
}
