package one.xis;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * All implementations of the annotated interface will be imported from other
 * the host framework e.g. Spring into xis private context.
 * <p>
 * TODO: Move to another module. This is a framework internal annotation.
 */
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface ImportInstances {
}
