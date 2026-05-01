package one.xis;

import java.lang.annotation.*;

/**
 * Annotation to pass parameters in query parameter style from page to a method.
 * It can be used for frontlets and forms.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface FrontletParameter {
    String value();
}
