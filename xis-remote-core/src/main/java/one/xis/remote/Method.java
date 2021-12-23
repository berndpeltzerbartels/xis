package one.xis.remote;

import java.lang.annotation.*;

/**
 * Name of the method is the value of this annotation or the name of the java-method,
 * if the value is empty.
 *
 * Method will be generated in client-javascript. The javascript-method contains only parameters
 * annotated wit @{@link Param}, but may have more arguments in java.
 *
 * Valid parameters in Java are:
 * <ul>
 *     <li>user-id, @see @{@link UserId}</li>
 *     <li>client-id, @see @{@link ClientId}</li>
 *     <li>state, @see {@link State}</li>
 *     <li>container, @see {@link Container}</li>
 * </ul>
 *
 *
 * @see Param
 *
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.SOURCE)
public @interface Method {
    String jsName() default "";
    boolean generated() default true;
}
