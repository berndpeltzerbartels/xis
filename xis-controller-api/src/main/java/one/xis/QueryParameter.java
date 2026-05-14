package one.xis;

import java.lang.annotation.*;

/**
 * Injects a query-string parameter into a model or action method.
 *
 * <p>For a request such as {@code /products.html?filter=active}, the value can
 * be read with {@code @QueryParameter("filter")}.</p>
 *
 * <pre>{@code
 * @ModelData
 * List<Product> products(@QueryParameter("filter") String filter) {
 *     return productService.findByFilter(filter);
 * }
 * }</pre>
 *
 * <p>Use {@link PathVariable} for variables declared in a {@link Page} path.</p>
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryParameter {
    String value();
}
