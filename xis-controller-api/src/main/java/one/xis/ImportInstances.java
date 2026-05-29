package one.xis;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Imports host-framework instances that implement the annotated interface into the private XIS context.
 *
 * <p>This is an advanced integration hook for libraries that bridge XIS with a host framework such as Spring. Normal
 * applications should prefer ordinary XIS components, constructor injection, and explicit configuration.</p>
 */
@Target({java.lang.annotation.ElementType.TYPE})
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface ImportInstances {
}
