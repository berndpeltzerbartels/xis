package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation allows reading and writing to a local storage.
 * <p>
 * The value is the key to the local storage. The annotation placed at a method parameter
 * will read the value from the local storage and place it into the parameter.
 * <p>
 * The annotation placed at a method will write the value of the parameter to the local storage.
 * This will typically be an action method, but it can also be used by model specific methods.
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalStorage {
    String value();
}
