package one.xis.server;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for methods whose return value should be written into a specific HTML tag or element with a given ID.
 * Either tagName or id must be set.
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TagContent {
    String tagName() default "";

    String id() default "";
}
