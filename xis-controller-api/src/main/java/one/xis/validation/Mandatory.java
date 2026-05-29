package one.xis.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Requires a submitted value to be present.
 *
 * <p>For strings, the value must contain non-blank text. For collections and arrays, the value must contain at least one
 * element. For other reference types, the value must be non-null.</p>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface Mandatory {

}
