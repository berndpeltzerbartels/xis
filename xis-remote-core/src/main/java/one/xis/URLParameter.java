package one.xis;

import java.lang.annotation.*;

/**
 * Method parameters annotated with {@link URLParameter} identify values from parent component
 * passed to a widget. The parent-component mirght be a parent widget or a page.
 * <p>
 * In the template these values must be decared with xis:parameter-attribute or a xis:parameter-tag.
 * <p>
 * These tags must be child-nodes of a widget-container.
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface URLParameter {
    String value();
}
