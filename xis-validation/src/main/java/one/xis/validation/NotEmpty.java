package one.xis.validation;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validates that a field is not null and not empty.
 * <p>
 * applicable types: String, Collection, Array
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@ValidatorClass(NotEmptyValidator.class)
public @interface NotEmpty {

}
