package one.xis.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * In case the anntatated field is of type collection, it will get validated
 * to contain at least the give number of elements.
 * <p>
 * Otherwise the corresponding field is validated to be filled.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MinLength {
    int value() default -1;
}
