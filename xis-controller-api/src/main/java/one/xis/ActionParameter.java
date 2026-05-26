package one.xis;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects a value submitted by the action element that triggered the current action.
 *
 * <p>Use this annotation for values passed by {@code xis:action} query strings,
 * child {@code <xis:parameter>} tags inside action links or buttons, drag and
 * drop action arguments, and form submitter parameters.</p>
 *
 * <p>Set either {@link #value()} for a named action parameter or {@link #index()}
 * for a positional action argument. Positional indexes are mainly useful for
 * function-style template expressions such as drag-and-drop action arguments.</p>
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionParameter {
    String value() default "";

    /**
     * Explicit 1-based positional action argument index.
     */
    int index() default -1;
}
