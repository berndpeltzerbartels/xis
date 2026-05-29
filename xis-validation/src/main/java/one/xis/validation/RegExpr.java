package one.xis.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Requires a value to match a regular expression.
 *
 * <p>The expression is interpreted by Java regular-expression matching. Use this for small, domain-specific input rules;
 * prefer a custom {@link Validator} when validation needs parsing, lookup, or business logic.</p>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Validate(validatorClass = RegExprValidator.class, messageKey = "validation.invalid", globalMessageKey = "validation.invalid.global")
public @interface RegExpr {
    /**
     * Java regular expression the value must match.
     */
    String value();
}
