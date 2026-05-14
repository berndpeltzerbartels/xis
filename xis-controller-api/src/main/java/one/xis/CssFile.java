package one.xis;

import java.lang.annotation.*;


/**
 * Adds a CSS file to a page or component.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
// TODO: may be remove it
public @interface CssFile {
    String value();
}
