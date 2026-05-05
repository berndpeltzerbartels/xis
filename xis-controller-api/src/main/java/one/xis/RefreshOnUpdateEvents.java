package one.xis;


import java.lang.annotation.*;

/**
 * Refreshes a page or frontlet when one of the configured update-event keys is
 * published.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RefreshOnUpdateEvents {
    String[] value() default {};
}
