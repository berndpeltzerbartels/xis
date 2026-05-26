package one.xis;

import java.lang.annotation.*;

/**
 * Binds a Java object to a form in the associated HTML page or frontlet.
 *
 * <p>When used on a method, the non-null return value is used to pre-fill the form.
 * When used on a method parameter, the submitted form data is injected into
 * the argument.</p>
 *
 * <p>Example:</p>
 *
 * <pre>{@code
 * @FormData("product")
 * Product initForm() {
 *     return new Product();
 * }
 *
 * @Action("save")
 * PageResponse save(@FormData("product") Product product) {
 *     repository.save(product);
 *     return PageResponse.of(SuccessPage.class);
 * }
 * }</pre>
 * <p>
 * If the value of the annotation is empty, the model will be accessible under the method name.
 * If the method is a getter (e.g. {@code getUser()}), the model will be accessible under the property name.
 */
@Documented
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FormData {

    String value();

    /**
     * Defines in which controller lifecycle phase this form value is loaded.
     *
     * <p>This attribute is only evaluated when {@code @FormData} is used on a
     * method to initialize a form. Action parameters annotated with
     * {@code @FormData} always receive submitted form values.</p>
     */
    ModelDataLoad load() default ModelDataLoad.ALWAYS;
}
