package one.xis;


import java.lang.annotation.*;

/**
 * Provides a fallback HTML template for a controller.
 *
 * <p>This is mainly useful for reusable libraries that ship a default template while still allowing an application to
 * override it with {@link HtmlFile} on the concrete controller class. If both annotations are present and the
 * {@code @HtmlFile} resource exists, the explicit template wins.</p>
 *
 * <p>Values starting with {@code /} are absolute classpath resource paths. Other values are resolved relative to the
 * controller package. The {@code .html} suffix may be omitted.</p>
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DefaultHtmlFile {
    /**
     * Default template path.
     */
    String value();
}
