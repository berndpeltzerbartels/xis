package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a value from client-side memory storage to an action method parameter.
 *
 * <p>This is a parameter annotation. XIS scans controller method parameters and
 * writes the referenced storage keys into the client configuration for the
 * current page or frontlet. The browser sends only those configured keys to the
 * server, not the whole client storage. The configured keys are sent for the
 * page/frontlet request even when the currently invoked action does not use
 * every key.</p>
 *
 * <p>The server deserializes the value for the key, passes it to the parameter,
 * and writes the parameter value back to client storage after method execution.
 * This makes the annotation most useful for mutable DTO-like values whose fields
 * are changed inside the action.</p>
 *
 * <p><strong>Example:</strong></p>
 * <pre>{@code
 * @Action("updatePreferences")
 * public void updatePreferences(@ClientStorage("userPreferences") UserPreferences prefs,
 *                               @ActionParameter("theme") String theme) {
 *     prefs.setTheme(theme);
 *     // the mutated prefs parameter is saved back to client storage
 * }
 * }</pre>
 *
 * <p><strong>Initialization:</strong><br>
 * If no value exists in storage, the parameter will be initialized with a default value.
 * For objects, this is typically a new instance. Use {@link NullAllowed} to allow null values instead.</p>
 *
 * <p><strong>Storage Location:</strong><br>
 * Data is stored client-side in JavaScript memory (as a field), not in browser storage APIs.
 * Unlike {@link LocalStorage} and {@link SessionStorage}, this data is not visible in browser
 * DevTools Storage tab. However, the data may not survive page reloads and is more
 * short-lived than sessionStorage.</p>
 *
 * <p><strong>Comparison:</strong></p>
 * <ul>
 *   <li>{@link LocalStorage}: Browser localStorage - persists across browser sessions</li>
 *   <li>{@link SessionStorage}: Browser sessionStorage - persists within tab, survives page reloads</li>
 *   <li>{@link ClientStorage}: JavaScript field - not visible in DevTools, may not survive page reloads</li>
 * </ul>
 *
 * <p><strong>Use Cases:</strong></p>
 * <ul>
 *   <li>Temporary data that should not be visible in browser DevTools Storage tab</li>
 *   <li>Short-lived state within a single page interaction</li>
 *   <li>Data that doesn't need to persist across page reloads</li>
 * </ul>
 *
 * @see LocalStorage
 * @see SessionStorage
 * @see NullAllowed
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientStorage {
    /**
     * The client-storage key to read from the browser and write back after
     * method execution.
     *
     * @return the storage key
     */
    String value();
}
