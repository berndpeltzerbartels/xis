package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as the primary data source for a page or widget.
 *
 * <p>The returned object will be made available as the model during rendering,
 * and is typically used to populate the corresponding HTML template.</p>
 *
 * <p>The {@code value()} specifies the name under which the model object will be
 * accessible in the template. This should match the data-binding expression in the HTML.</p>
 *
 * <p>Each page or widget must have exactly one {@code @ModelData} method.
 * The method can take parameters (e.g. {@code @RequestScope} or {@code @LocalStorage})
 * which are resolved before rendering.</p>
 *
 * <pre>{@code
 * @ModelData("product")
 * public Product loadProduct(@RequestScope Product product) {
 *     return product;
 * }
 * }</pre>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ModelData {
    String value() default ""; // The name under which the model object will be accessible in the template
}
