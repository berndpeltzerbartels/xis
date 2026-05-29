package one.xis.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class or composed annotation as an application service.
 *
 * <p>{@code @Service} is a semantic stereotype for {@link Component}. Use it for business services and other
 * non-UI components that should be discovered by package scanning and injected into other components.</p>
 */

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
public @interface Service {
}
