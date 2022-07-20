package one.xis;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be used on method-level or on parameter-level and inidcates the object is part of the
 * client-state of the widget or page (annotation is used in both contexts).
 * <ul>
 *     <li><strong>Method-Level</strong> Method returns a model-type.
 *     Data is submitted to the client and corresponding entity on client side
 *     will get relaced by the new version. That means all previously assigned attributes
 *     are getting lost if the return-value is newly created entity.</li>
 *
 *     <li><strong>Parameter-Level</strong> Parameter represents the actual client state of an
 *     entity and allows you to read and edit data. After leaving method, data is submitted to
 *     the client and only updated values will be changed in client state</li>
 * </ul>
 * <p>
 *     The annotation-value in this annotation is indented to identify different objects of
 *     the page's or widget's client state and equal to attribute names of the data object.
 * <p>
 *     This is typically used, when you create multiple data-objects in seperate methods.
 *     Having no value is typically the case when using a single model for the widget/page. If this is the case,
 *     the fields of the model are equal to attribute names of the data object on client-side.
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Model {
    String value() default "";
}
