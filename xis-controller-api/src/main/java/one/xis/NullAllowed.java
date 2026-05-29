package one.xis;

import java.lang.annotation.*;


/**
 * Allows XIS to inject {@code null} into a controller method parameter.
 *
 * <p>By default, framework-supplied parameters and storage values are usually created or converted before invocation.
 * Add this annotation when {@code null} is a meaningful value, for example when a missing client-side storage value
 * should be handled explicitly by the action method.</p>
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface NullAllowed {

}
