package one.xis;


import java.lang.annotation.*;

/**
 * Annotation to specify the default HTML file for a controller.
 * This file will be used when no specific HTML file is defined for the controller.
 * <p>
 * if (path starts with a slash, it is considered absolute, otherwise relative to the controller's package.
 * For example:
 * <pre>
 *  * @HtmlFile("TestPage.html") // file is inside controller's package
 *  * @HtmlFile("/test/TestPage.html") // absolute path
 *  * </pre>
 */
// If the value starts with a slash, it is considered an absolute path; otherwise, it
// is relative to the controller's package.
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DefaultHtmlFile {
    String value();
}
