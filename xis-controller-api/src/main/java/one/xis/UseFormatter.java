package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Uses a custom {@link Formatter} for parsing submitted values and formatting displayed values.
 *
 * <p>Apply this to form fields, record components, action parameters, or a custom composed annotation when the default
 * conversion is not precise enough for a domain type or presentation format.</p>
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface UseFormatter {
    /**
     * Formatter implementation used for the annotated value.
     */
    Class<? extends Formatter<?>> value();
}
