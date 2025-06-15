package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark methods or classes that require specific roles for access control.
 * It can be used in conjunction with security frameworks to enforce role-based access control.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Roles {
    /**
     * The roles required to access the annotated method or class.
     *
     * @return an array of role names
     */
    String[] value() default {};
}
