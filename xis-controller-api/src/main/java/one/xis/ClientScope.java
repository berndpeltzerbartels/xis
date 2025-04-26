package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a temporary value to a JavaScript variable on the client side,
 * scoped to the current page instance.
 *
 * <p><strong>Usage on method parameters:</strong><br>
 * The value will be injected into the JavaScript environment and made available
 * as a variable with the specified key during rendering or interaction.
 *
 * <pre>{@code
 * public String render(@ClientScope("status") String status) {
 *     return "Status: " + status;
 * }
 * }</pre>
 *
 * <p><strong>Usage on methods:</strong><br>
 * The method's return value will be made available as a page-scoped variable
 * in the JavaScript context during the current page lifecycle.
 *
 * <pre>{@code
 * @PageScope("status")
 * public String getStatus() {
 *     return "active";
 * }
 * }</pre>
 *
 * <p><strong>Note:</strong> Page scope is client-side and only persists during the current
 * page session in memory. It will be lost on a full page reload or browser navigation,
 * and is not shared across tabs or persisted in storage.</p>
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientScope {
    String value();
}
