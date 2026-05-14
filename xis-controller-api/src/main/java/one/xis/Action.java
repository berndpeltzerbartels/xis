package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a controller method as callable from a template action.
 *
 * <p>An action can be triggered by an action link, an action button, or a form
 * submit. Parameters can come from the current page URL, query string, form
 * data, or child {@code <xis:parameter>} elements.</p>
 *
 * <p>The return value controls navigation or frontlet replacement. A
 * {@code void} action stays on the current page or frontlet and refreshes the
 * model data.</p>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Action {
    String value() default "";

    String[] updateEventKeys() default {};
}
