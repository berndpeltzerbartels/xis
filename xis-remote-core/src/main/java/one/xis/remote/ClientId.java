package one.xis.remote;

import java.lang.annotation.*;

/**
 * Parameters annotated with {@link ClientId} must be of type {@link String}.
 * Value is an initial random-string submitted with any request from client.
 * In contrary to {@link UserId}, this value does not require a login.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface ClientId {
}
