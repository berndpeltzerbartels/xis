package one.xis;

import java.lang.annotation.*;

/**
 * Binds a Java object to a form in the associated HTML page or widget.
 *
 * <p>When used on a method, the return value is used to pre-fill the form.
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
 */
@Documented
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface FormData {

    String value();
}
