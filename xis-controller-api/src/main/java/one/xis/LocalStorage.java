package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a value from the browser's LocalStorage to a method parameter,
 * or stores a method's return value in LocalStorage.
 *
 * <p><strong>Usage on method parameters:</strong><br>
 * The value associated with the given key will be read from LocalStorage
 * and passed into the method as an argument.
 *
 * <pre>{@code
 * public String render(@LocalStorage("username") String name) {
 *     return "Hello " + name;
 * }
 * }</pre>
 *
 * <p><strong>Usage on methods:</strong><br>
 * The return value of the method will be stored in LocalStorage under the given key
 * after the request has been processed.
 *
 * <pre>{@code
 * @LocalStorage("username")
 * public String getNameToStore() {
 *     return currentUser.getName();
 * }
 * }</pre>
 *
 * <p>Note: Storage is handled client-side in the user's browser. Values are automatically
 * restored during later page requests. All values are serialized as JSON.</p>
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface LocalStorage {
    String value();
}
