package one.xis;

import java.lang.annotation.*;

/**
 * Annotation to bind a method parameter to a path variable in a URL.
 * Requires the corresponding page defines the variable in it's {@link Page} annotation.
 * <p>
 * Example:
 * <p>
 * <pre>
 * &#064;Page("/users/{id}")
 * class UserPage {
 *     public Response getUser(@PathVariable("id") String userId) {
 *         // ...
 *     }
 * }
 * </pre>
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PathVariable {
    String value();
}
