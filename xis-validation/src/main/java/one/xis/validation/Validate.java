package one.xis.validation;

import one.xis.deserialize.PostProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Meta-annotation for building a custom validation annotation.
 *
 * <p>The annotation points XIS to the validator class and to the message keys
 * used for field-specific and global validation messages. Put {@code @Validate}
 * on your own annotation, then use that annotation on form fields, record
 * components, or action parameters.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@PostProcessor(ValidationPostProcessor.class)
public @interface Validate {
    /**
     * Validator implementation for the annotated value.
     */
    Class<? extends Validator<?>> validatorClass();

    /**
     * Message key used for field-bound validation messages.
     */
    String messageKey() default "";

    /**
     * Message key used for global validation messages.
     */
    String globalMessageKey() default "";
}
