package one.xis.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for fields and parameters to validate against a regular expression.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Validate(validatorClass = MinLengthValidator.class, messageKey = "validation.invalid", globalMessageKey = "validation.invalid.global")
public @interface RegExpr {
    String value();
}
