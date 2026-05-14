package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark methods or classes that require specific roles for access control.
 * If used without role names, it requires an authenticated user but no named role. Prefer {@link Authenticated} for
 * that case, because it states the intent more clearly.
 * It can be used in conjunction with security frameworks to enforce role-based access control.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Roles {
    /**
     * The roles required to access the annotated method or class.
     * An empty array means that authentication is required but no named role is required. Prefer
     * {@link Authenticated} for authenticated-only access.
     *
     * @return an array of role names
     */
    String[] value() default {};
}
