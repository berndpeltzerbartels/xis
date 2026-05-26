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
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ActionParameter {
    String value() default "";

    /**
     * Explicit 1-based positional action argument index. Leave at {@code -1}
     * to consume the next positional action value.
     */
    int index() default -1;
}
