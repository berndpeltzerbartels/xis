package one.xis.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Registers a component as a replaceable default implementation.
 *
 * <p>This is mainly useful for libraries and framework extensions that provide
 * defaults while still allowing an application component to override them.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface DefaultComponent {
}
