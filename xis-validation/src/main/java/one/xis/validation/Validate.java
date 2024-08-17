package one.xis.validation;

import one.xis.deserialize.PostProcessor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE})
@PostProcessor(ValidationPostProcessor.class)
public @interface Validate {
    Class<? extends Validator<?>> validatorClass();

    String messageKey() default "";

    String globalMessageKey() default "";
}
