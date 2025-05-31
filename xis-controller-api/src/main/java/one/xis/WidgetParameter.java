package one.xis;

import java.lang.annotation.*;

/**
 * Annotation to pass parameters in query parameter style from page to a method.
 * It can be used for widgets and forms.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface WidgetParameter {
    String value();
}
