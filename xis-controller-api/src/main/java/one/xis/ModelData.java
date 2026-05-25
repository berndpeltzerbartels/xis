package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a data source for a page or frontlet.
 *
 * <p>The returned object will be made available as the model during rendering,
 * and is typically used to populate the corresponding HTML template.</p>
 *
 * <p>The {@code value()} specifies the name under which the model object will be
 * accessible in the template. This should match the data-binding expression in the HTML.</p>
 *
 * <p>A page or frontlet can have zero or more {@code @ModelData} methods.
 * Each method exposes one value. The method can take parameters
 * (for example {@code @PathVariable}, {@code @QueryParameter},
 * {@code @SharedValue}, or {@code @LocalStorage})
 * which are resolved before rendering.</p>
 *
 * <pre>{@code
 * @ModelData("product")
 * public Product loadProduct(@SharedValue("product") Product product) {
 *     return product;
 * }
 * }</pre>
 * If the value of the annotation is empty, the model will be accessible under the method name.
 * If the method is a getter (e.g. {@code getUser()}), the model will be accessible under the property name.
 *
 * <p>{@link #load()} can narrow when the method is called. The default is
 * {@link ModelDataLoad#ALWAYS}.</p>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ModelData {
    String value() default ""; // The name under which the model object will be accessible in the template

    /**
     * Alias for {@link #value()} with a more explicit name for examples where the
     * model key represents a template variable.
     */
    String varName() default "";

    /**
     * Defines in which controller lifecycle phase this model value is loaded.
     */
    ModelDataLoad load() default ModelDataLoad.ALWAYS;
}
