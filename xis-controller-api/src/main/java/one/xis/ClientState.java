package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a value from client-side state to an action method parameter or method return value.
 *
 * <p>XIS scans controller method parameters and annotated methods and writes
 * the referenced storage keys into the client configuration for the
 * current page or frontlet. The browser sends only those configured keys to the
 * server, not the whole client state. The configured keys are sent for the
 * page/frontlet request even when the currently invoked action does not use
 * every key.</p>
 *
 * <p>The server deserializes the value for the key, passes it to the parameter,
 * and writes the parameter value or annotated method return value back to
 * client state after method execution.
 * This makes the annotation most useful for mutable DTO-like values whose fields
 * are changed inside the action.</p>
 *
 * <p>{@code @ClientState} is a convenience for short-lived UI state such as a
 * selected item, an expanded panel, or temporary form context. It is not the
 * default place for application variables. Prefer model data, UI parameters,
 * shared values, and normal server-side state when those express the flow
 * directly.</p>
 *
 * <p><strong>Example:</strong></p>
 * <pre>{@code
 * @Action("updatePreferences")
 * public void updatePreferences(@ClientState("userPreferences") UserPreferences prefs,
 *                               @ActionParameter("theme") String theme) {
 *     prefs.setTheme(theme);
 *     // the mutated prefs parameter is saved back to client state
 * }
 * }</pre>
 *
 * <p><strong>Initialization:</strong><br>
 * If no value exists in client state, the parameter will be initialized with a default value.
 * For objects, this is typically a new instance. Use {@link NullAllowed} to allow null values instead.</p>
 *
 * <p>Returning {@code null} from an annotated method, or writing back a nullable
 * parameter value, removes the value from client state.</p>
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
 *   <li>{@link ClientState}: JavaScript field - not visible in DevTools, may not survive page reloads</li>
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
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientState {
    /**
     * The client-state key to read from the browser and write back after
     * method execution.
     *
     * @return the storage key
     */
    String value();
}
