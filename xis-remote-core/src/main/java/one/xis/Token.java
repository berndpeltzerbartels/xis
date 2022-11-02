package one.xis;

import java.lang.annotation.*;

/**
 * Parameters annotated with {@link Token} must be of type {@link String}
 * with 12 characters.
 * <p>
 * Value is submitted with any request from client and stored in local storage of the browser.
 * <p>
 * In contrary to {@link UserId}, this value does not require a login.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface Token {
}
