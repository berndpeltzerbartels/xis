package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a value from the browser's localStorage to an action method parameter.
 *
 * <p>This is a parameter annotation. XIS scans controller method parameters and
 * writes the referenced storage keys into the client configuration for the
 * current page or frontlet. The browser sends only those configured keys to the
 * server, not the whole localStorage. The configured keys are sent for the
 * page/frontlet request even when the currently invoked action does not use
 * every key.</p>
 *
 * <p>The server deserializes the value for the key, passes it to the parameter,
 * and writes the parameter value back to localStorage after method execution.
 * This makes the annotation most useful for mutable DTO-like values whose fields
 * are changed inside the action.</p>
 *
 * <p><strong>Example:</strong></p>
 * <pre>{@code
 * @Action("addToCart")
 * public void addToCart(@LocalStorage("cart") ShoppingCart cart,
 *                       @ActionParameter("productId") String productId) {
 *     cart.addProduct(productId);
 *     // the mutated cart parameter is saved back to localStorage
 * }
 * }</pre>
 *
 * <p><strong>Initialization:</strong><br>
 * If no value exists in localStorage, the parameter will be initialized with a default value.
 * For objects, this is typically a new instance. Use {@link NullAllowed} to allow null values instead.</p>
 *
 * <p><strong>Storage Location:</strong><br>
 * Data is stored client-side in the browser's localStorage and persists across browser sessions.
 * All values are serialized as JSON.</p>
 *
 * @see SessionStorage
 * @see ClientStorage
 * @see NullAllowed
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalStorage {
    /**
     * The localStorage key to read from the browser and write back after method
     * execution.
     *
     * @return the storage key
     */
    String value();
}
