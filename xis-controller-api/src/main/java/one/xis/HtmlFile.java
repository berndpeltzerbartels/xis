package one.xis;

import java.lang.annotation.*;

/**
 * Maps a page, frontlet, modal, or include controller to an explicit HTML template.
 *
 * <p>You do not need this annotation when the template follows the default convention: the HTML file has the same simple
 * name as the controller class and is located in the same package. Use {@code @HtmlFile} when the name differs, when
 * several controllers share one template, or when the template lives in a shared resource folder.</p>
 *
 * <p>Values starting with {@code /} are absolute classpath resource paths. Other values are resolved relative to the
 * controller package. The {@code .html} suffix may be omitted.</p>
 *
 * <pre>{@code
 * @HtmlFile("CustomerForm.html")        // relative to the controller package
 * @HtmlFile("/templates/customer.html") // absolute classpath resource path
 * }</pre>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HtmlFile {
    /**
     * Template path.
     */
    String value();
}
