package one.xis.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.RECORD_COMPONENT})
@Validate(validatorClass = EMailValidator.class, messageKey = "validation.email", globalMessageKey = "validation.email.global")
public @interface EMail {
}
