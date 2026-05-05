package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Reads or writes a named server-side value shared in the current XIS scope.
 *
 * <p>Use this for state that belongs on the server. Browser-side state is
 * handled by {@link LocalStorage}, {@link SessionStorage}, and
 * {@link ClientStorage}.</p>
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SharedValue {
    String value();
}
