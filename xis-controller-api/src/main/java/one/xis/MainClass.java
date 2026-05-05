package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Legacy application metadata for identifying an application main class.
 *
 * <p>Normal applications should use the runtime setup described in the user
 * documentation.</p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
// TODO may be remove it
public @interface MainClass { // TODO noch erforderlich ?
}
