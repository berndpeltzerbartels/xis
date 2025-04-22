package one.xis;

import java.lang.annotation.*;

/**
 * Used to map a javascript-file to a controller in case, the name is different. Intention is to
 * allow sharing javascript-files with different controllers. In case the names are equal and the javascript-file
 * is located in the controller's package (e.g. Xyz.java / Xyz.js), there is no need to use this annotation.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JavascriptFile {
    String value();
}
