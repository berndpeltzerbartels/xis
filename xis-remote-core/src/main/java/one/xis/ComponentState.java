package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ComponentState {
    /**
     * Key of for value of the component-state. If empty, the
     * name of the parameter-variables is the key.
     *
     * @return key of component-state
     */
    String value() default "";
}
