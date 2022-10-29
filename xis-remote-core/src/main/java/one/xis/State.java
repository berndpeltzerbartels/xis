package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.PARAMETER, ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface State {
    /**
     * An optional id of the model instance. Must be set, in case
     * of the model-class is not identifiable by its type, e.g.
     * the model is jaust a string.
     *
     * @return the id of the model
     */
    String value() default "";
}
