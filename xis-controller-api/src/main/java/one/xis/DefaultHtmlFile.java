package one.xis;


import java.lang.annotation.*;

/**
 * Annotation for Libraries providing a default HTML file for controllers, but allows overriding it
 * by using the using {@link HtmlFile} annotation on specific controller classes.
 * <p>
 * Intension is to create libraries with default HTML files that can be customized by the user.
 * <p>
 * If the value starts with a slash, it is considered an absolute path; otherwise, it
 * is relative to the controller's package.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DefaultHtmlFile {
    String value();
}
