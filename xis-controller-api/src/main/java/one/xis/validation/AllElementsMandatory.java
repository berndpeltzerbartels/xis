package one.xis.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Requires every element of a collection or array value to be present.
 *
 * <p>Use this together with {@link Mandatory} when the collection itself must exist and each contained element must be
 * non-null. Primitive arrays cannot contain {@code null}; the annotation is mainly useful for object arrays and
 * collections.</p>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllElementsMandatory {
}
