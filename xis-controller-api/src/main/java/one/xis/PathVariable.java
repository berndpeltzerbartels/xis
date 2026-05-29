package one.xis;

import java.lang.annotation.*;

/**
 * Injects a variable from the current page URL into a page, frontlet, modal, model, or action method parameter.
 *
 * <p>The variable must be declared in the active {@link Page} path, for example
 * {@code @Page("/users/{id}.html")}. Frontlets on that page can also read the page path variables without declaring a
 * separate frontlet URL. For plain HTTP controllers, use {@code one.xis.http.PathVariable} instead.</p>
 *
 * <pre>{@code
 * @Page("/users/{id}.html")
 * class UserPage {
 *     @ModelData("user")
 *     User user(@PathVariable("id") long userId) {
 *         return users.find(userId);
 *     }
 * }
 * }</pre>
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathVariable {
    /**
     * Name of the placeholder in the page URL.
     */
    String value();
}
