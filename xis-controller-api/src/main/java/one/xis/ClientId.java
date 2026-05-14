package one.xis;

import java.lang.annotation.*;

/**
 * Marks a parameter as the anonymous client identifier used to associate
 * actions with a specific browser session, without requiring the user to log in.
 *
 * <p>Values must be of type {@link String} and exactly 12 characters long.</p>
 *
 * <p>The client ID is automatically submitted with every request and persisted
 * in the browser's local storage, allowing continuity across sessions and reloads.</p>
 *
 * <p>Unlike {@link UserId}, {@code ClientId} does not imply authentication.
 * It is primarily used to track non-logged-in users — for example, to associate
 * shopping carts or form progress with a specific device or browser.</p>
 *
 * <p>This allows for user-friendly experiences where a permanent account is not
 * required — e.g. in shops that support guest checkout or payment via external providers like PayPal.</p>
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientId {
}
