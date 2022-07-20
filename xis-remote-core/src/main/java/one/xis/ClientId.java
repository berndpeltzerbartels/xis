package one.xis;

import java.lang.annotation.*;

/**
 * Parameters annotated with {@link ClientId} must be of type {@link String}
 * as long no custom ClientIdFactory was created.
 * Value is submitted with any request from client.
 * In contrary to {@link UserId}, this value does not require a login.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.SOURCE)
public @interface ClientId {
}
