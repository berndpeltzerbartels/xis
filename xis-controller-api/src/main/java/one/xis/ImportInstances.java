package one.xis;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Imports host-framework instances into the private XIS context.
 *
 * <p>This is an advanced integration hook for libraries that bridge XIS with a host framework such as Spring. Normal
 * applications should prefer ordinary XIS components, constructor injection, and explicit configuration.</p>
 *
 * <p>When used on an interface or class, matching host-framework beans are imported by assignability. When used on an
 * annotation, host-framework beans annotated with that annotation are imported.</p>
 */
@Target({java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.ANNOTATION_TYPE})
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface ImportInstances {
}
