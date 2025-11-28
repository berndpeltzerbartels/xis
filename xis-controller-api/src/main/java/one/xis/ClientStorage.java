package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a value from server-side client storage to a method parameter,
 * or stores a method's return value in server-side client storage.
 *
 * <p>Unlike {@link LocalStorage} and {@link SessionStorage} which store data
 * in the browser, ClientStorage maintains data on the server per client/session.
 * This makes it suitable for sensitive data, larger datasets, or data that should
 * not be accessible via browser DevTools.</p>
 *
 * <p><strong>Usage on method parameters:</strong><br>
 * The value associated with the given key will be read from server-side client storage
 * and passed into the method as an argument.
 *
 * <pre>{@code
 * public String render(@ClientStorage("userPreferences") UserPreferences prefs) {
 *     return "Theme: " + prefs.getTheme();
 * }
 * }</pre>
 *
 * <p><strong>Usage on methods:</strong><br>
 * The return value of the method will be stored in server-side client storage under
 * the given key after the request has been processed.
 *
 * <pre>{@code
 * @ClientStorage("userPreferences")
 * public UserPreferences getPreferences() {
 *     return new UserPreferences("dark", "en");
 * }
 * }</pre>
 *
 * <p><strong>Update Events:</strong><br>
 * Changes to client storage do not automatically trigger updates in other components.
 * Use {@link Action#updateEventKeys()} to emit events, and {@link RefreshOnUpdateEvents}
 * on widget classes to listen for those events.
 *
 * <pre>{@code
 * @Action(value = "updatePreferences", updateEventKeys = {"prefs-changed"})
 * @ClientStorage("userPreferences")
 * public UserPreferences savePreferences(@ActionParameter("theme") String theme) {
 *     return new UserPreferences(theme, "en");
 * }
 * }</pre>
 *
 * @see LocalStorage
 * @see SessionStorage
 * @see RefreshOnUpdateEvents
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientStorage {
    /**
     * The key under which the value is stored in server-side client storage.
     *
     * @return the storage key
     */
    String value();
}
