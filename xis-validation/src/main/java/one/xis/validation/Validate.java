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
 * used for field-specific and global validation messages.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@PostProcessor(ValidationPostProcessor.class)
public @interface Validate {
    Class<? extends Validator<?>> validatorClass();

    String messageKey() default "";

    String globalMessageKey() default "";
}
