package one.xis.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * In case the annotated field is of type collection, it will get validated
 * to contain elements.
 * <p>
 * Otherwise the corresponding field is validated to be filled.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Mandatory {
    String value() default "";
}
