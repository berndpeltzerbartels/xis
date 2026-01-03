package one.xis.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that an annotated class is a service.
 * <p>
 * Such classes are considered as candidates for auto-detection
 * when using annotation-based configuration and classpath scanning.
 * <p>
 * Similar to spring's @Service annotation, this is just an alias
 * for @{@link Component}
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface Service {
}
