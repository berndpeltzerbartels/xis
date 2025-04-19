package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The return value of the annotated method will be used stored in the page context
 * by the key specified in the annotation value.
 * <p>
 * The annotation placed at a method parameter will read the value from the page context.
 * The behaviour is similar to the {@link LocalStorage} annotation, but the page context
 * is not persistent and will be lost when the page is reloaded.
 * <p>
 * It is intended to share data between the pages and widgets.
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PageContext {
    String value();
}
