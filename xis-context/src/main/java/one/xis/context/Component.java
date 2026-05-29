package one.xis.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class or composed annotation as a XIS context component.
 *
 * <p>Component classes are candidates for package scanning, dependency injection, lifecycle callbacks, and constructor
 * injection. Higher-level stereotypes such as {@link Service} and controller annotations build on this annotation.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface Component {
}
