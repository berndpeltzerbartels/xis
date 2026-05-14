package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Reads or writes a named browser-side database value.
 *
 * <p>This is an advanced client-state feature. Most applications should keep
 * state on the server unless browser-side persistence is explicitly needed.</p>
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
// TODO : may be remove it
public @interface LocalDatabase {
    String value();
}
