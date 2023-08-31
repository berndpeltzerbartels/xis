package one.xis;

import java.lang.annotation.*;

/**
 * Used to map a html-template to a controller in case, the name is different. Intention is to
 * allow sharing html-files with different controllers. In case the names are equal and the html-file
 * is located in the controller's package (e.g. Xyz.java / Xyz.html), there is no need to use this annotation.
 */
@Target({ElementType.TYPE, ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HtmlFile {
    String value();
}
