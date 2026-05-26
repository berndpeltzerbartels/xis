package one.xis;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Injects a stable parameter of the current modal instance.
 *
 * <p>Modal parameters come from a modal URL such as
 * {@code EditCustomerModal?customerId=42}, child {@code <xis:parameter>} tags
 * on the element opening the modal, or an explicit {@link ModalResponse}. They
 * describe the context of the loaded modal and are kept while that modal is
 * processed.</p>
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModalParameter {
    String value() default "";
}
