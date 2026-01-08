package one.xis;

import java.lang.annotation.*;

/**
 * Used to map a html-template to a controller in case, the name is different. Intention is to
 * allow sharing html-files with different controllers. In case the names are equal and the html-file
 * is located in the controller's package (e.g. Xyz.java / Xyz.html), there is no need to use this annotation.
 * <p>
 * if (path starts with a slash, it is considered absolute, otherwise relative to the controller's package.
 * For example:
 * <pre>
 * &#064;HtmlFile("TestPage.html") // file is inside controller's package
 * &#064;HtmlFile("/test/TestPage.html")  // absolute path
 * </pre>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HtmlFile {
    String value();
}
