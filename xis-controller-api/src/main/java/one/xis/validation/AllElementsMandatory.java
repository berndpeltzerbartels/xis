package one.xis.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for collection fields and array fields or parameters, only.
 * For fields with this annotation, all elements are validated to non-null.
 * <p>
 * In case the element type is a primitive type, the validation will fail without this annotation, too.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllElementsMandatory {
}
