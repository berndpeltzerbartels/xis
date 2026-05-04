package test.page.forms.validation.annotation;

import one.xis.validation.Validate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Validate(validatorClass = NotNegativeValidator.class, messageKey = "validation.notNegative", globalMessageKey = "validation.notNegative.global")
public @interface NotNegative {
}
