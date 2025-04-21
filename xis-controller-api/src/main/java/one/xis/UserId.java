package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method parameter as the user identifier of a currently logged-in user.
 *
 * <p>This annotation is used to inject the user ID associated with the active session
 * and should be applied to parameters of type {@link String}.</p>
 *
 * <p>The actual source of the user ID depends on the authentication module in use:
 * it may be extracted from an identity token (e.g. Keycloak) or from a custom
 * password-based login system.</p>
 *
 * <p>This annotation is intended for use in authenticated contexts where actions
 * need to be associated with a specific user account.</p>
 *
 * <p>In contrast to {@link ClientId}, which identifies anonymous users,
 * {@code UserId} refers to a verified identity.</p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface UserId {
}
