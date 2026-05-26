package one.xis;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects a stable parameter of the current frontlet instance.
 *
 * <p>Frontlet parameters come from a frontlet URL such as
 * {@code ProductDetails?productId=42}, child {@code <xis:parameter>} tags on a
 * frontlet container or frontlet link, or an explicit {@link FrontletResponse}.
 * They describe the context of the loaded frontlet and are kept across actions
 * until a frontlet response changes them.</p>
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface FrontletParameter {
    String value() default "";
}
