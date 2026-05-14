package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a page, frontlet, action method, action parameter, or action DTO as requiring an authenticated user.
 * <p>
 * Use {@code @Authenticated} when login is required but no named application role is needed. Use {@link Roles} when the
 * annotated element requires one of the listed role names.
 */
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Authenticated {
}
