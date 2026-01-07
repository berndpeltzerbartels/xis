package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a value from the browser's localStorage to an action method parameter.
 *
 * <p>The client sends only the specified localStorage key to the server.
 * The server deserializes the value, passes it to the method, and any modifications
 * made to the object are automatically saved back to localStorage after method execution.</p>
 *
 * <p><strong>Example:</strong></p>
 * <pre>{@code
 * @Action("addToCart")
 * public void addToCart(@LocalStorage("cart") ShoppingCart cart,
 *                       @ActionParameter("productId") String productId) {
 *     cart.addProduct(productId);
 *     // cart is automatically saved back to localStorage
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
     * The localStorage key to read from and write to.
     *
     * @return the storage key
     */
    String value();
}
